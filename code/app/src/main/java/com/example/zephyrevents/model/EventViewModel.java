package com.example.zephyrevents.model;
import androidx.lifecycle.ViewModel;

/**
 * Data holder ViewModel with public fields to track state of an event while creating or editing
 */
public class EventViewModel extends ViewModel {
    // Edit Mode Tracking
    public boolean isEditMode = false;
    public String eventId = null;
    public boolean isDataLoaded = false;
    public int originalApplicants = 0;
    public String organizerId = null;

    // Form Data
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