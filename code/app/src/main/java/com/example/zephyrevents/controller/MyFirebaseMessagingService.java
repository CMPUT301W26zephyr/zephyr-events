package com.example.zephyrevents.controller;

import androidx.annotation.NonNull;
import com.example.zephyrevents.util.LocalNotificationHelper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Receives Firebase push messages and shows local notifications when allowed by user settings.
 * Refreshes the device token with the server when FCM issues a new one.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    /**
     * Handles incoming Firebase data messages based on user notification settings
     * - FCM delivers a data message
     * - Drop the message if notification setting is turn off
     * - Or show local notification with payload’s title, body, and event id.
     * @param remoteMessage Remote message that has been received.
     */

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData() != null && remoteMessage.getData().size() > 0) {
            String type = remoteMessage.getData().get("type");

            // Check Local SharedPreferences
            android.content.SharedPreferences prefs = getSharedPreferences("notification_preference", MODE_PRIVATE);

            if (type != null) {
                if ((type.equals("WON_EVENT") || type.equals("LOST_EVENT") || type.equals("LOTTERY_COMPLETED"))
                        && !prefs.getBoolean("lottery_results", true)) {
                    return;
                }
                if (type.equals("MANUAL") && !prefs.getBoolean("organizer_announcement", true)) {
                    return;
                }
            }

            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            String eventId = remoteMessage.getData().get("eventId");

            if (title == null) title = "Lottofy";

            int uniqueId = (int) System.currentTimeMillis();
            LocalNotificationHelper.showNotification(this, title, body, uniqueId, eventId);
        }
    }

    /**
     * Keeps the servers copy of the device FCM token up to date when Firebase makes a new one
     * @param token The token used for sending messages to this application instance.
     */

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        new UserController(this).syncFcmToken();
    }
}