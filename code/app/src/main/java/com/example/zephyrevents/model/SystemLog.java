package com.example.zephyrevents.model;

import com.example.zephyrevents.util.GenerateId;

public class SystemLog {
    private String id;
    private long timestamp;
    private String actionType;
    private String description;
    private String actorName;

    public SystemLog() {}

    public SystemLog(String actionType, String description, String actorName) {
        this.id = GenerateId.getUniqueId();
        this.timestamp = System.currentTimeMillis();
        this.actionType = actionType;
        this.description = description;
        this.actorName = actorName;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }
}