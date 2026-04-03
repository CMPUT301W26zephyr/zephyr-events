package com.example.zephyrevents.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.zephyrevents.R;
import com.example.zephyrevents.view.MainActivity;

public class LocalNotificationHelper {

    private static final String CHANNEL_ID = "zephyr_events_channel";

    public static void showNotification(Context context, String title, String message, int notificationId, String eventId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Event Notifications", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for lotteries and event updates");
            notificationManager.createNotificationChannel(channel);
        }

        // Send to WelcomeActivity which will route based on the eventId
        Intent intent = new Intent(context, com.example.zephyrevents.view.WelcomeActivity.class);
        if (eventId != null && !eventId.isEmpty()) {
            intent.putExtra("eventId", eventId);
        } else {
            intent.putExtra("TARGET_TAB", "MyEvents");
        }
        intent.putExtra("FROM_NOTIFICATION", true);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, notificationId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_24)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        notificationManager.notify(notificationId, builder.build());
    }
}