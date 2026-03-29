package com.example.zephyrevents.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.zephyrevents.R;
import com.example.zephyrevents.view.UserNotificationListView;

public class LocalNotificationHelper {

    private static final String CHANNEL_ID = "zephyr_events_channel";

    public static void showNotification(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Android 8.0+ requires a Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Event Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for lotteries and event updates");
            notificationManager.createNotificationChannel(channel);
        }

        // Create the Intent to open the Notifications Screen when clicked
        Intent intent = new Intent(context, UserNotificationListView.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the OS notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_24) // Your bell icon
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true) // Dismiss when clicked
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Make it pop up on screen
                .setContentIntent(pendingIntent);

        // Show the notification using a unique ID so they don't overwrite each other
        int uniqueId = (int) System.currentTimeMillis();
        notificationManager.notify(uniqueId, builder.build());
    }
}