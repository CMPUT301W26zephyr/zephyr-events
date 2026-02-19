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
    private String UserTimeZone; // Should be something like America/Edmonton
    User(String name, String email, String phone, boolean notificationsOptOut, String UserTimeZone){
        this.deviceId = GenerateId.getUniqueId();
        this.email = email;
        this.location = location;
        this.phone = phone;
        this.notificationsOptOut = notificationsOptOut;
        this.UserTimeZone = UserTimeZone;
        this.name = name;
    }

    // constructor with default timezone
    public User(String name, String email, String phone, boolean notificationsOptOut ){
        this(name, email, phone, notificationsOptOut, TimeZone.getDefault().getID());
    }
    // constructor with default (false) notificationsOptOut
    public User(String name, String email, String phone,  String UserTimeZone ){
        this(name, email, phone, false, UserTimeZone);
    }
    // constructor with default (false) notificationsOptOut and default timezone
    public User(String name, String email, String phone ){
        this(name, email, phone, false, TimeZone.getDefault().getID());
    }



}
