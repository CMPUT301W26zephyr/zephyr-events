package com.example.zephyrevents.controller;

import androidx.annotation.NonNull;
import com.example.zephyrevents.util.LocalNotificationHelper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // This triggers only when the app is actively in the FOREGROUND
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            if (title == null) title = "Zephyr Events";

            int uniqueId = (int) System.currentTimeMillis();
            LocalNotificationHelper.showNotification(this, title, body, uniqueId);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        new UserController(this).syncFcmToken();
    }
}