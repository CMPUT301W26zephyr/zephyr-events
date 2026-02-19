package com.example.zephyrevents.model;

import com.example.zephyrevents.util.GenerateId;

import java.util.TimeZone;

/**
 * This is a class that defines a user.
 */
public class User {
    private String deviceId;
    private String email;
    private String location; // Should this be a string? or lat lon? I'm leaning string.
    private String name;
    private boolean notificationsOptOut; // Opt out of all notifications account wide?
    private String phone;
    private String userTimeZone; // Should be something like America/Edmonton
    public User(String name, String email, String phone, boolean notificationsOptOut, String userTimeZone){
        this.deviceId = GenerateId.getUniqueId();
        this.email = email;
        this.location = location;
        this.phone = phone;
        this.notificationsOptOut = notificationsOptOut;
        this.userTimeZone = userTimeZone;
        this.name = name;
    }

    // constructor with default timezone
    public User(String name, String email, String phone, boolean notificationsOptOut ){
        this(name, email, phone, notificationsOptOut, TimeZone.getDefault().getID());
    }
    // constructor with default (false) notificationsOptOut
    public User(String name, String email, String phone,  String userTimeZone ){
        this(name, email, phone, false, userTimeZone);
    }
    // constructor with default (false) notificationsOptOut and default timezone
    public User(String name, String email, String phone ){
        this(name, email, phone, false, TimeZone.getDefault().getID());
    }

    public String getDeviceId() {
        return deviceId;
    }

    // DeviceID should stay immutable, setter added because of firebase deserialization.
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public void setNotificationsOptOut(boolean notificationsOptOut) {
        this.notificationsOptOut = notificationsOptOut;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserTimeZone() {
        return userTimeZone;
    }

    public void setUserTimeZone(String userTimeZone) {
        userTimeZone = userTimeZone;
    }
}
