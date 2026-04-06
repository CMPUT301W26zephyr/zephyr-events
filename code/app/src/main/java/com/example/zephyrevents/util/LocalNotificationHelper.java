package com.example.zephyrevents.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.zephyrevents.R;
import com.example.zephyrevents.view.auth.WelcomeActivity;

/**
 * Builds and posts a high-priority local notifications
 */
public class LocalNotificationHelper {

    private static final String CHANNEL_ID = "zephyr_events_channel";

    /**
     * Ensures the notification channel exists and then shows a notification that launches the welcome flow.
     * @param context used for {@link android.app.NotificationManager} and the tap {@link Intent}
     * @param title notification title
     * @param message body text
     * @param notificationId unique id for this notification
     * @param eventId if non-null and non-empty, passed as {@code eventId}; otherwise opens the My Events tab
     */

    public static void showNotification(Context context, String title, String message, int notificationId, String eventId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Event Notifications", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for lotteries and event updates");
            notificationManager.createNotificationChannel(channel);
        }

        // Send to WelcomeActivity which will route based on the eventId
        Intent intent = new Intent(context, WelcomeActivity.class);
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
