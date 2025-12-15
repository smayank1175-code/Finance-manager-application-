// File Path: app/src/main/java/com/example/financemanager/NotificationHelper.java

package com.example.financemanager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {
    private static final String CHANNEL_ID = "finance_manager_channel";
    private static final String CHANNEL_NAME = "Transaction Alerts";

    // Notification Channel बनाता है (Android O और उससे ऊपर के लिए)
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Alerts for newly added or updated financial transactions.");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    // नोटिफिकेशन दिखाता है
    public static void showNotification(Context context, String title, String message, int notificationId) {
        // सुनिश्चित करें कि चैनल बन गया है
        createNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                // सुनिश्चित करें कि आपके पास splash_icon नाम की drawable resource है
                .setSmallIcon(R.drawable.splash_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            // notificationId को हर बार unique रखें
            manager.notify(notificationId, builder.build());
        }
    }
}