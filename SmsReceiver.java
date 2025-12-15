// File Path: app/src/main/java/com/example/financemanager/SmsReceiver.java

package com.example.financemanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";


    private static final String[] BANK_SENDERS = {
            "BANK", "HDFC", "SBI", "AXIS", "ICICI", "KOTAK", "PNB",


            "UNIONB", "JM-", "VK-",


            "IPPB", "INDPOST", "JD-", "JX-", "JK-", "IPBMSG",


            "CP-", "GROWWO", "VM-", "AM-", "DM-", "TXN-", "CA-", "AD-", "BW-",


            "PAYTM", "GPE", "PHONEPE", "UPI", "WALLET"
    };

    // Amount Pattern
    private static final String REGEX_AMOUNT = "(?:rs|inr|Rs|INR|RS|INR)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)";
    private static final Pattern PATTERN_AMOUNT = Pattern.compile(REGEX_AMOUNT, Pattern.CASE_INSENSITIVE);

    // Expense Keywords
    private static final String REGEX_EXPENSE = "debited|spent|wdl|withdrawn|paid|transfer\\s*to|purchase|sent|txn|atm|deducted";
    private static final Pattern PATTERN_EXPENSE = Pattern.compile(REGEX_EXPENSE, Pattern.CASE_INSENSITIVE);

    // Income Keywords
    private static final String REGEX_INCOME = "credited|received|deposit|refund|added|transfer\\s*in|salary";
    private static final Pattern PATTERN_INCOME = Pattern.compile(REGEX_INCOME, Pattern.CASE_INSENSITIVE);

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {

            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null && pdus.length > 0) {
                    SmsMessage[] messages = new SmsMessage[pdus.length];
                    String sender = null;
                    String messageBody = "";

                    for (int i = 0; i < pdus.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        if (sender == null) {
                            sender = messages[i].getOriginatingAddress();
                        }
                        // Long messages को जोड़ें
                        messageBody += messages[i].getMessageBody();
                    }

                    boolean isBankSms = false;
                    if (sender != null) {
                        String senderUpper = sender.toUpperCase(Locale.getDefault());
                        for (String bank : BANK_SENDERS) {
                            if (senderUpper.contains(bank)) {
                                isBankSms = true;
                                break;
                            }
                        }
                    }

                    Log.d(TAG, "Sender: " + sender + ", IsBankSms: " + isBankSms);

                    if (isBankSms) {
                        Log.i(TAG, "SMS received from bank sender: " + sender);

                        Matcher amountMatcher = PATTERN_AMOUNT.matcher(messageBody);

                        if (amountMatcher.find() &&
                                (PATTERN_EXPENSE.matcher(messageBody).find() || PATTERN_INCOME.matcher(messageBody).find())) {


                            String amountStr = amountMatcher.group(1).replace(",", "").trim();
                            long amount = 0;
                            try {

                                amount = (long) Double.parseDouble(amountStr);
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Error parsing amount: " + e.getMessage());
                                return;
                            }

                            boolean isIncome = PATTERN_INCOME.matcher(messageBody).find();

                            // Category & Title Logic
                            String title = isIncome ? "Bank Transfer/Credit" : "Bank Withdrawal/Debit";
                            String category = "Others";

                            String lowerCaseBody = messageBody.toLowerCase(Locale.getDefault());

                            if (isIncome && lowerCaseBody.contains("salary")) {
                                title = "Salary";
                                category = "Salary";
                            } else if (lowerCaseBody.contains("upi")) {
                                title = isIncome ? "UPI Receive" : "UPI Payment";
                                category = "UPI/Digital";
                            } else if (!isIncome && lowerCaseBody.contains("purchase")) {
                                title = "Shopping/Purchase";
                                category = "Shopping";
                            } else if (!isIncome && lowerCaseBody.contains("atm")) {
                                title = "ATM Withdrawal";
                                category = "Cash";
                            } else if (!isIncome && lowerCaseBody.contains("bill")) {
                                title = "Bill Payment";
                                category = "Bills";
                            }


                            String senderUpper = sender.toUpperCase(Locale.getDefault());
                            if (senderUpper.contains("GROWWO") || senderUpper.contains("CP-")) {
                                title = "Investment/Groww Debit";
                                category = "Investment";
                            } else if (senderUpper.contains("UNIONB")) {
                                title = isIncome ? "Union Bank Credit" : "Union Bank Debit";
                                category = "Others";
                            }


                            // Description सेट करें (200 अक्षरों तक सीमित)
                            String desc = messageBody.length() > 200 ? messageBody.substring(0, 200) + "..." : messageBody;

                            if (amount > 0) {
                                saveTransaction(context, title, category, amount, desc, isIncome);
                            } else {
                                Log.e(TAG, "FAILURE: Amount is zero or not properly parsed.");
                            }
                        } else {
                            Log.e(TAG, "FAILURE: RegEx Did NOT Match the SMS Body or Amount is zero! Body: " + messageBody);
                        }
                    } else {
                        Log.i(TAG, "SMS is not from a recognised bank sender.");
                    }
                }
            }
        }
    }

    private void saveTransaction(Context context, String title, String category, long amount, String desc, boolean isIncome) {
        FinanceTable financeTable = new FinanceTable();

        financeTable.setTitle(title);
        financeTable.setCategory(category);
        financeTable.setAmount(amount);
        financeTable.setDescription(desc);
        financeTable.setIncome(isIncome);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        financeTable.setDate(dateFormat.format(Calendar.getInstance().getTime()));

        try {
            FinanceDatabase financeDatabase = FinanceDatabase.getInstance(context);
            financeDatabase.getDao().insertFinance(financeTable);

            // Notification को show करें (NotificationHelper को लागू किया गया है)
            String type = isIncome ? "Income" : "Expense";
            String notificationMessage = String.format("Auto-Added: %s of Rs %d (%s)", type, amount, title);
            NotificationHelper.showNotification(context, "New SMS Transaction", notificationMessage, (int) System.currentTimeMillis());

            Toast.makeText(context, type + " of Rs " + amount + " auto-added! (" + title + ")", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            // Database Error का Log
            Log.e(TAG, "Database save failed: " + e.getMessage());
            Toast.makeText(context, "Auto-tracking failed: Database Error.", Toast.LENGTH_LONG).show();
        }
    }
}