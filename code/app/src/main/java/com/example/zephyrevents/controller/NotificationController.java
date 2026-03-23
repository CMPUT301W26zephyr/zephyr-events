package com.example.zephyrevents.controller;

import com.example.zephyrevents.model.Notification;
import com.example.zephyrevents.model.NotificationType;
import com.example.zephyrevents.repository.NotificationRepository;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.model.NotifyingGroup;
import java.util.List;

// TODO: Actually implement notifications how? (Scheduled with Lottery control on Cloud Functions?)
/**
 * Controller that manages logic related to Notifications.
 * Handles automatic system notifications and manual event-based notifications
 */
public class NotificationController {

    private final NotificationRepository notificationRepository;

    /**
     * Default constructor.
     */
    public NotificationController() {
        notificationRepository = new NotificationRepository();
    }

    /**
     * Creates and saves an automatic system notification for a specific user.
     * @param userId  User ID (recipient).
     * @param eventId Associated Event ID.
     * @param type    The NotificationTyp (enum).
     * @param text    The message content of the notification
     */
    public void sendAutomaticNotification(String userId, String eventId, NotificationType type, String text) {

        Notification notification = new Notification(
                userId,
                eventId,
                type,
                text,
                false,
                false
        );

        notificationRepository.saveNotification(notification, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                System.out.println("Auto notification saved");
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("Auto notification failed");
            }
        });
    }

    // helper function
    // in the waitlist tabs from organizer, they are going to send notification by groups.
    /**
     * Sends a group-specific notification to a list of users based on their status in an event.
     * Maps a NotifyingGroup (enum) to a predefined message, calls manual notifications for each user.
     * @param userIds  A list of user IDs to be notified.
     * @param eventId  The ID of the associated event.
     * @param group    The category of users being notified (determines message content).
     */
    public void notifyUsers(List<String> userIds, String eventId, NotifyingGroup group) {

        if (userIds == null || userIds.isEmpty()) {
            System.out.println("No users to notify");
            return;
        }

        String message;

        switch (group) {

            case WAITLIST:
                message = "You are currently on the waitlist for this event.";
                break;

            case WINNERS:
                message = "Congratulations! You have been selected for this event.";
                break;

            case UNREGISTERED:
                message = "You were not selected for this event.";
                break;

            case FINAL_LIST:
                message = "You are confirmed for the event.";
                break;

            default:
                message = "Event notification.";
        }

        for (String userId : userIds) {
            sendManualNotification(userId, eventId, message);
        }
    }

    /**
     * Private helper function for NotifyUsers(). for custom message.
     * Sets to NotificationType.MANUAL.
     * @param userId    User ID (recipient).
     * @param eventId   Associated Event ID.
     * @param text      Text content.
     */
    private void sendManualNotification(String userId, String eventId, String text) {

        Notification notification = new Notification(
                userId,
                eventId,
                NotificationType.MANUAL,
                text,
                false,
                false
        );

        notificationRepository.saveNotification(notification, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                System.out.println("Notification saved");
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("Failed to save notification");
            }
        });
    }

    // when user clicked notification icon, they can see all the notifications in 'my events'

    /**
     * Retrieves all notifications for a specific user to be displayed in the UI.
     * To be used in "My Events"
     * @param userId    User ID.
     * @param callback  RepositoryCallback that passes results back.
     */
    public void getUserNotifications(String userId, RepositoryCallback<List<Notification>> callback) {

        notificationRepository.getUserNotifications(userId, new RepositoryCallback<List<Notification>>() {

            @Override
            public void onSuccess(List<Notification> result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

}