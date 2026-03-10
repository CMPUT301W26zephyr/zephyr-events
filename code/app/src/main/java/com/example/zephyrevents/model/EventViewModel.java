package com.example.zephyrevents.model;

import androidx.lifecycle.ViewModel;

public class EventViewModel extends ViewModel {
    public String title = "";
    public String type = "";
    public String price = "";
    public String description = "";
    public String waitlistCapacity = "";
    public String attendeeCount = "";

    public String location = "";
    public String address = "";
    public boolean requireGeolocation = true;

    public String registrationPeriod = "";
    public String eventDate = "";
}