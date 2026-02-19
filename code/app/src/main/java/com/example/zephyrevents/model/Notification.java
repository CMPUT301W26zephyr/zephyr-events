package com.example.zephyrevents.model;

import com.example.zephyrevents.util.GenerateId;

public class Notification {

    String notificaitonId;
    String userId;
    String eventId;
    long time;
    NotificationType type;

    String text;
    boolean sent;
    boolean read;

    Notification(
            String userId,
            String eventId,
            NotificationType type,
            String text,
            boolean sent,
            boolean read
    ){

        this.notificaitonId = GenerateId.getUniqueId();
        this.userId = userId;
        this.eventId = eventId;
        this.type = type;
        this.text = text;
        this.sent = sent;
        this.read = read;
    }

}
