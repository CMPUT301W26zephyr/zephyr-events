// file name: NotificationRepository
// for save, track notification data

package com.example.zephyrevents.repository;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.model.Notification;

import java.util.ArrayList;
import java.util.List;

import com.example.zephyrevents.model.NotificationType;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;


//public Notification(
//        String userId,
//        String eventId,
//        NotificationType type,
//        String text,
//        boolean sent,
//        boolean read,
//        String notificationId
//
//)

public class NotificationRepository {

    private final FirebaseFirestore db;
    private static final String TAG = "NotificationRepository";

    public NotificationRepository() {
        db = FirebaseFirestore.getInstance();
    }
    public NotificationRepository(FirebaseFirestore db) {
        this.db = db;
    }

    // Below is 'Create' part in CRUD using set()
    // Create -
    public void saveNotification(Notification notification, RepositoryCallback<Void> callback) {
        if (notification == null) {
            var e = new IllegalArgumentException("Notification cannot be null");
            Log.w(TAG, "notification passed had value of null", e);
            callback.onFailure(e);
            return;
        }

        if (notification.getNotificationId() == null || notification.getNotificationId().trim().isEmpty()) {
            var e = new IllegalArgumentException("Notification id passed has no value");
            Log.w(TAG, "Notification has no associated id", e);
            callback.onFailure(e);
            return;
        }

        db.collection(Collections.NOTIFICATIONS)
                .document(notification.getNotificationId())
                .set(notification)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Firestore notification added object id: " + notification.getNotificationId());
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        callback.onFailure(e);
                    }
                });
    }

    // Below is 'Read' part in CRUD using get()
    // in controller, we call notificationRepository.getNotificationId(notificationId, callback);
    public void getNotificationId(String notificationId, RepositoryCallback<Notification> callback) {
        if (notificationId == null) {
            var e = new IllegalArgumentException("Notification id cannot be null");
            Log.w(TAG, "Notification passed had value of null", e);
            callback.onFailure(e);
            return;
        }

        if (notificationId.trim().isEmpty()) {
            var e = new IllegalArgumentException("Notification id passed has no value");
            Log.w(TAG, "Notification has no associated id", e);
            callback.onFailure(e);
            return; // exit before network call.
        }

        db.collection(Collections.NOTIFICATIONS)
                .document(notificationId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Notification result = documentSnapshot.toObject(Notification.class);
                    callback.onSuccess(result);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // Below is 'Update' part in CRUD using update()
    // in controller, we call notificationRepository.updateNotification(notification, callback);
    public void updateNotification(Notification notification, RepositoryCallback<Void> callback) {
        if (notification == null) {
            var e = new IllegalArgumentException("Notification cannot be null");
            Log.w(TAG, "Notification passed had value of null", e);
            callback.onFailure(e);
            return;
        }

        if (notification.getNotificationId() == null || notification.getNotificationId().trim().isEmpty()) {
            var e = new IllegalArgumentException("Notification id passed has no value");
            Log.w(TAG, "Notification has no associated id", e);
            callback.onFailure(e);
            return;
        }

        db.collection(Collections.NOTIFICATIONS)
                .document(notification.getNotificationId())
                .update(
                        "userId", notification.getUserId(),
                        "eventId", notification.getEventId(),
                        "type", notification.getType(),
                        "text", notification.getText(),
                        "sent", notification.isSent(),
                        "read", notification.isRead()
                )
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Firestore notification updated id: " + notification.getNotificationId());
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error updating notification", e);
                    callback.onFailure(e);
                });
    }
    // Below is 'Delete' part in CRUD using delete()
    // in controller, we call notificationRepository.deleteNotification(notificationId, callback);
    public void deleteNotification(String notificationId, RepositoryCallback<Void> callback) {
        if (notificationId == null) {
            var e = new IllegalArgumentException("Notification id cannot be null");
            Log.w(TAG, "Notification passed had value of null", e);
            callback.onFailure(e);
            return;
        }

        if (notificationId.trim().isEmpty()) {
            var e = new IllegalArgumentException("Notification id passed has no value");
            Log.w(TAG, "Notification has no associated id", e);
            callback.onFailure(e);
            return;
        }

        db.collection(Collections.NOTIFICATIONS)
                .document(notificationId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Firestore notification deleted id: " + notificationId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error deleting notification", e);
                    callback.onFailure(e);
                });
    }

    // when a user want to see all the notifications that received from system (when clicked icon)
    public void getUserNotifications(String userId, RepositoryCallback<List<Notification>> callback) {

        if (userId == null || userId.trim().isEmpty()) {
            callback.onFailure(new IllegalArgumentException("UserId cannot be null"));
            return;
        }

        db.collection(Collections.NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .orderBy("time", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    List<Notification> notifications = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Notification notification = doc.toObject(Notification.class);
                        notifications.add(notification);
                    }

                    callback.onSuccess(notifications);
                })
                .addOnFailureListener(callback::onFailure);
    }
}

