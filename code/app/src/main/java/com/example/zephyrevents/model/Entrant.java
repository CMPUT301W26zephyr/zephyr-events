package com.example.zephyrevents.model;

/**
 * Data holder with public fields used to represent Entrants in Views/EntrantAdapter
 */
public class Entrant {
    /** Firestore user id (opens public profile when the row is tapped). */
    public String userId;
    /** Profile image URL from {@link User#getAvatarUrl()} when available. */
    public String avatarUrl;
    public String name;
    public String detail;
    public boolean showCancel;

    public Entrant(String userId, String name, String detail, boolean showCancel, String avatarUrl) {
        this.userId = userId;
        this.name = name;
        this.detail = detail;
        this.showCancel = showCancel;
        this.avatarUrl = avatarUrl;
    }
}
