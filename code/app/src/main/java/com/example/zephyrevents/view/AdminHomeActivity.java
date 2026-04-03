package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zephyrevents.R;

public class AdminHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminhome);

        // Existing menu buttons
        TextView userProfiles = findViewById(R.id.userProfiles);
        TextView browseImages = findViewById(R.id.browseImages);
        TextView browseEvents = findViewById(R.id.browseEvents);
        TextView systemNotifications = findViewById(R.id.systemNotifications);
        TextView statistics = findViewById(R.id.statistics);
        TextView systemLogs = findViewById(R.id.systemLogs);
        TextView termsConditions = findViewById(R.id.termsConditions);

        // Back button
        findViewById(R.id.button_back2).setOnClickListener(v -> finish());

        // Navigation to admin features
        userProfiles.setOnClickListener(v ->
                startActivity(new Intent(this, AdminBrowseProfilesActivity.class)));

        browseImages.setOnClickListener(v ->
                startActivity(new Intent(this, AdminBrowseImageActivity.class)));

        browseEvents.setOnClickListener(v ->
                startActivity(new Intent(this, AdminBrowseEventActivity.class)));

        systemNotifications.setOnClickListener(v ->
                startActivity(new Intent(this, AdminSystemNotificationActivity.class)));

        statistics.setOnClickListener(v ->
                startActivity(new Intent(this, AdminStatisticsActivity.class)));

        systemLogs.setOnClickListener(v ->
                startActivity(new Intent(this, AdminSystemLogActivity.class)));

        termsConditions.setOnClickListener(v ->
                startActivity(new Intent(this, AdminTermsConditionsActivity.class)));

        // Setup bottom navbar (same behavior as MainActivity)
        setupBottomNav();
    }

    // Handle navbar navigation
    private void setupBottomNav() {

        // Home
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("TARGET_TAB", "Home");
            startActivity(intent);
        });

        // My Events
        findViewById(R.id.nav_my_events).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("TARGET_TAB", "MyEvents");
            startActivity(intent);
        });

        // Create Event
        findViewById(R.id.nav_create_event).setOnClickListener(v ->
                startActivity(new Intent(this, OrganizerEventAddEditView.class)));

        // Scan QR
        findViewById(R.id.nav_scan_qr).setOnClickListener(v ->
                startActivity(new Intent(this, QrScannerActivity.class)));

        // Profile
        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("TARGET_TAB", "ProfileView");
            startActivity(intent);
        });
    }
}