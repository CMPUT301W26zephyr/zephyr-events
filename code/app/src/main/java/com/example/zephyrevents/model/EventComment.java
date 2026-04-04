package com.example.zephyrevents.model;

import com.google.firebase.firestore.Exclude;

/**
 * A comment or reply on an event. Stored in Firestore so it persists across sessions.
 * Top-level comments have {@code parentCommentId == null}; replies reference the parent document id.
 */
public class EventComment {

    private String id;
    private String eventId;
    private String userId;
    /** Denormalized for display; written once when the comment is created. */
    private String authorName;
    /** Profile image URL at post time; keeps avatars consistent with the user profile. */
    private String authorAvatarUrl;
    private String body;
    private long createdAt;
    /** Firestore id of parent comment, or null for a top-level comment. */
    private String parentCommentId;

    public EventComment() {}

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorAvatarUrl() {
        return authorAvatarUrl;
    }

    public void setAuthorAvatarUrl(String authorAvatarUrl) {
        this.authorAvatarUrl = authorAvatarUrl;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(String parentCommentId) {
        this.parentCommentId = parentCommentId;
    }
}
