package com.example.zephyrevents.model;

/**
 * This is a class that encapsulates user contact info. i.e. email, phone number...
 * Can later be expanded easily if more methods are needed.
 * Used as a nested object in the user class.
 */
public class ContactInfo {
    private String email;
    private String phone;

    /**
     * No arg constructor for Firebase.
     */
    public ContactInfo() {};

    /**
     * Constructor with parameters email and phone number.
     *
     * @param email The user's email
     * @param phone The user's phone number
     */
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
