package com.example.zephyrevents.model;

/**
 * This is a class that encapsulates user contact info. i.e. email, phone number...
 * Can later be expanded easily if more methods are needed.
 * Used as an nested object in the user class.
 */
public class ContactInfo {
private String email;
private String phone;

// no arg constructor for firebase
public ContactInfo() {};

public ContactInfo(String email, String phone) {
    this.email = email;
    this.phone = phone;
}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
