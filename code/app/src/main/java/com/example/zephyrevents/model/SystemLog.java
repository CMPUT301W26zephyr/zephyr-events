package com.example.zephyrevents.model;

import com.example.zephyrevents.util.GenerateId;

/**
 * An audit style log entry: who did what, when, with a unique id.
 */
public class SystemLog {
    private String id;
    private long timestamp;
    private String actionType;
    private String description;
    private String actorName;

    public SystemLog() {}

    /**
     * Creates a log with a new id and the current time as {@link #timestamp}.
     * @param actionType  category of the action (e.g. admin or system event type)
     * @param description what happened
     * @param actorName who did the action
     */

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