package com.example.zephyrevents.model;

import com.example.zephyrevents.util.GenerateId;

public class Notification {

    String notificationId;
    String userId;
    String eventId;
    long time;
    NotificationType type;

    String text; // Should text be generated later based on the type?
    boolean sent;
    boolean read;

    // gonna adjust this class later and add more constructors when its stable.
    public Notification(
            String userId,
            String eventId,
            NotificationType type,
            String text,
            boolean sent,
            boolean read
    ){

        this.notificationId = GenerateId.getUniqueId();
        this.userId = userId;
        this.eventId = eventId;
        this.type = type;
        this.text = text;
        this.sent = sent;
        this.read = read;
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
