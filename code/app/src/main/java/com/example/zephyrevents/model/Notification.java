package com.example.zephyrevents.model;

import com.example.zephyrevents.util.GenerateId;

/**
 * This is a class that defines a notification.
 */
public class Notification {

    String notificationId;
    String userId;
    String eventId;
    long time;
    NotificationType type;

    String text; // Should text be generated later based on the type?
    boolean sent;
    boolean read;

    // no arg constructor for firebase
    public Notification() {}

    /**
     * Default constructor (NOTE: NEED TO RESOLVE CONSISTENCY WITH ID GENERATION)
     * @param userId
     * @param eventId
     * @param type
     * @param text
     * @param sent
     * @param read
     * @param notificationId
     */
    public Notification(
            String userId,
            String eventId,
            NotificationType type,
            String text,
            boolean sent,
            boolean read,
            String notificationId

    ){
        this.notificationId = notificationId;
        this.userId = userId;
        this.eventId = eventId;
        this.type = type;
        this.text = text;
        this.sent = sent;
        this.read = read;
        this.time = System.currentTimeMillis();
    }

    // constructor but auto generate id.
    public Notification(
            String userId,
            String eventId,
            NotificationType type,
            String text,
            boolean sent,
            boolean read
            ){
        this(userId, eventId, type, text, sent, read, GenerateId.getUniqueId());
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
