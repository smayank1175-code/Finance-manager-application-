package com.example.financemanager;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.financemanager.databinding.ActivityAddBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class addActivity extends AppCompatActivity {

    ActivityAddBinding binding;

    private static final String[] CATEGORIES = new String[]{
            "Salary", "Investment", "Others",
            "Food", "Travel", "Shopping", "Rent", "Bills", "Groceries", "Cash"
    };

    private boolean update = false;
    private int id;
    private String currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        currentDate = dateFormat.format(Calendar.getInstance().getTime());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, CATEGORIES);
        binding.categoryInput.setAdapter(adapter);

        update = getIntent().getBooleanExtra("update", false);
        if (update) {
            binding.addText.setText("UPDATE TRANSACTION");
            id = getIntent().getIntExtra("id", -1);
            binding.amountInput.getEditText().setText(String.valueOf(getIntent().getLongExtra("amount", 0)));
            binding.descriptionInput.getEditText().setText(getIntent().getStringExtra("description"));

            binding.titleInput.getEditText().setText(getIntent().getStringExtra("title"));
            binding.categoryInput.setText(getIntent().getStringExtra("category"), false);

            boolean isIncome = getIntent().getBooleanExtra("isIncome", false);
            if (isIncome) {
                binding.incomeRadio.setChecked(true);
            } else {
                binding.expenseRadio.setChecked(true);
            }

            currentDate = getIntent().getStringExtra("date");
        } else {
            binding.addText.setText("ADD TRANSACTION");
        }

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.addText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amount = binding.amountInput.getEditText().getText().toString().trim();
                String desc = binding.descriptionInput.getEditText().getText().toString().trim();

                String title = binding.titleInput.getEditText().getText().toString().trim();
                String category = binding.categoryInput.getText().toString().trim();

                if (TextUtils.isEmpty(amount)) {
                    Toast.makeText(addActivity.this, "Enter Amount!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(title)) {
                    title = "No Title";
                }
                if (TextUtils.isEmpty(category)) {
                    category = "Others";
                }

                long finalAmount = Long.parseLong(amount);
                boolean isIncome = binding.incomeRadio.isChecked();

                FinanceTable financeTable = new FinanceTable();
                financeTable.setAmount(finalAmount);
                financeTable.setDescription(desc);
                financeTable.setIncome(isIncome);

                financeTable.setTitle(title);
                financeTable.setCategory(category);

                if (!update) {
                    financeTable.setDate(currentDate);
                } else {
                    financeTable.setId(id);
                    financeTable.setDate(currentDate);
                }

                FinanceDatabase financeDatabase = FinanceDatabase.getInstance(view.getContext());
                FinanceDao financeDao = financeDatabase.getDao();

                if (!update){
                    financeDao.insertFinance(financeTable);

                    // ✅ NEW: Notification Show करें (Insert)
                    String notificationMessage = String.format("Manual Transaction Added: %s (Rs %d)", title, finalAmount);
                    // unique ID के लिए System.currentTimeMillis() का उपयोग करें
                    NotificationHelper.showNotification(view.getContext(), "Transaction Added", notificationMessage, (int) System.currentTimeMillis());

                    Toast.makeText(addActivity.this, "Transaction Added!", Toast.LENGTH_SHORT).show();
                } else {
                    financeDao.updateFinance(financeTable);

                    // ✅ NEW: Notification Show करें (Update)
                    String notificationMessage = String.format("Transaction Updated: %s (Rs %d)", title, finalAmount);
                    // Update के लिए ID का उपयोग करें
                    NotificationHelper.showNotification(view.getContext(), "Transaction Updated", notificationMessage, id);

                    Toast.makeText(addActivity.this, "Transaction Updated!", Toast.LENGTH_SHORT).show();
                }

                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}