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


/**
 * Repository class for managing Notification data in Firebase Firestore.
 * Provides CRUD functionality for notification objects, allows users to retrieve notification history.
 */
public class NotificationRepository {

    private final FirebaseFirestore db;
    private static final String TAG = "NotificationRepository";

    /**
     * Default constructor. Uses the production Firestore instance.
     */
    public NotificationRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Constructor with dependency injection for testing.
     * @param db  The injected firestore instance.
     */
    public NotificationRepository(FirebaseFirestore db) {
        this.db = db;
    }


    /**
     * Saves a new notification to the database.
     * @param notification  THe notification object to be saved
     * @param callback      Handles completion (fail -> exception).
     */
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

    /**
     * Retrieves a specific notification by its ID.
     * in controller, we call notificationRepository.getNotificationId(notificationId, callback);
     * @param notificationId The notification ID.
     * @param callback       Callback returning the Notification object
     */
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

    /**
     * Updates fields of an existing notification (e.g., marking as read or sent).
     * in controller, we call notificationRepository.updateNotification(notification, callback);
     * @param notification  The notification object with updated data.
     * @param callback      Callback upon completion
     */
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


    /**
     * Deletes a notification from the database.
     * in controller, we call notificationRepository.deleteNotification(notificationId, callback);
     * @param notificationId The notification ID.
     * @param callback      Callback upon completion
     */
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

    /**
     * Finds all unread notifications for a user and marks them as read.
     * This instantly turns off the red notification dot on the UI.
     */
    public void markAllAsRead(String userId) {
        db.collection(Collections.NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) return;

                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        batch.update(doc.getReference(), "read", true);
                    }
                    batch.commit();
                });
    }

    // when a user want to see all the notifications that received from system (when clicked icon)

    /**
     * Retrieves all notifications sent to a specific user, ordered by time descending.
     * @param userId   The user ID.
     * @param callback Callback returns List of Notification objects.
     */
    public void getUserNotifications(String userId, RepositoryCallback<List<Notification>> callback) {

        if (userId == null || userId.trim().isEmpty()) {
            callback.onFailure(new IllegalArgumentException("UserId cannot be null"));
            return;
        }

        db.collection(Collections.NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    List<Notification> notifications = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Notification notification = doc.toObject(Notification.class);
                        notifications.add(notification);
                    }

                    notifications.sort((n1, n2) -> Long.compare(n2.getTime(), n1.getTime()));
                    callback.onSuccess(notifications);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Remove all notifications for a given user
     * @param userId id of user whose notifications to be deleted
     * @param callback called when the operation failed or finishes
     */

    public void deleteAllUserNotifications(String userId, RepositoryCallback<Void> callback) {
        db.collection(Collections.NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        batch.delete(doc.getReference());
                    }
                    batch.commit().addOnSuccessListener(v -> callback.onSuccess(null))
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }
}

