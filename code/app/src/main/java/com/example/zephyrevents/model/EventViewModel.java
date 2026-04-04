package com.example.zephyrevents.model;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.ViewModel;
import android.net.Uri;


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
    public String price = "";
    public String description = "";
    public String waitlistCapacity = "";
    public String attendeeCount = "";
    public String location = "";
    public String address = "";
    public boolean requireGeolocation = false;
    public double geolocationRadiusKm = 0.5; // default 500m
    public double eventLat;
    public double eventLng;
    public String registrationPeriod = "";
    public String eventDate = "";

    /** False = public listing; true = private (hidden from browse, no promo QR). */
    public boolean privateEvent = false;

    public List<String> coOrganizerUserIds = new ArrayList<>();
    public List<String> pendingPrivateWaitlistInviteUserIds = new ArrayList<>();

    public Uri pendingEventImageUri = null;

    public String existingImgUrl = "";
}