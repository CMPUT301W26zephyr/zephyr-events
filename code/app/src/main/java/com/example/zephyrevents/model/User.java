package com.example.zephyrevents.model;

import com.example.zephyrevents.util.GenerateId;

import java.util.TimeZone;

/**
 * This is a class that defines a user.
 */
public class User {
    private ContactInfo contactInfo;
    private String id;
    private String location; // Should this be a string? or lat lon? I'm leaning string.
    private String name;
    private boolean notificationsOptOut; // Opt out of all notifications account wide?
    private String userTimeZone; // Should be something like America/Edmonton
    private String avatarUrl; // Firebase Storage store URL for profile image
    private String fcmToken;

    /**
     * Firebase no arg constructor
     */
    public User() {}

    /**
     * Constructor with all parameters; generates a unique ID.
     * @param name
     * @param email
     * @param phone
     * @param notificationsOptOut
     * @param userTimeZone
     * @param location
     * @param avatarUrl
     */
    public User(String name, String email, String phone, boolean notificationsOptOut, String userTimeZone, String location, String avatarUrl){
        this.contactInfo = new ContactInfo(email, phone);
        this.id = GenerateId.getUniqueId();
        this.location = location;
        this.name = name;
        this.notificationsOptOut = notificationsOptOut;
        this.userTimeZone = userTimeZone;
        this.avatarUrl = avatarUrl;
    }

    /**
     * Constructor with default timezone
     * @param name
     * @param email
     * @param phone
     * @param notificationsOptOut
     * @param location
     * @param avatarUrl
     */
    public User(String name, String email, String phone, boolean notificationsOptOut, String location, String avatarUrl ){
        this(name, email, phone, notificationsOptOut, TimeZone.getDefault().getID(), location, avatarUrl);
    }
    /**
     * constructor with default notificationsOptOut setting (false)
     * @param name
     * @param email
     * @param phone
     * @param userTimeZone
     * @param location
     * @param avatarUrl
     */
    public User(String name, String email, String phone,  String userTimeZone, String location, String avatarUrl ){
        this(name, email, phone, false, userTimeZone, location, avatarUrl);
    }
    /**
     * constructor with default notificationsOptOut setting (false) AND default timezone
     * @param name
     * @param email
     * @param phone
     * @param location
     * @param avatarUrl
     */
    public User(String name, String email, String phone, String location, String avatarUrl){
        this(name, email, phone, false, TimeZone.getDefault().getID(), location, avatarUrl);
    }

    public String getId() {
        return id;
    }

    // DeviceID should stay immutable, setter added because of firebase deserialization.
    public void setId(String id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isNotificationsOptOut() {
        return notificationsOptOut;
    }

    public void setNotificationsOptOut( boolean notificationsOptOut) {
        this.notificationsOptOut = notificationsOptOut;
    }


    public String getUserTimeZone() {
        return userTimeZone;
    }

    public void setUserTimeZone(String userTimeZone) {
        this.userTimeZone = userTimeZone;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
