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

        TextView userProfiles = findViewById(R.id.userProfiles);
        TextView browseImages = findViewById(R.id.browseImages);
        TextView browseEvents = findViewById(R.id.browseEvents);
        TextView systemNotifications = findViewById(R.id.systemNotifications);
        TextView statistics = findViewById(R.id.statistics);
        TextView systemLogs = findViewById(R.id.systemLogs);
        TextView termsConditions = findViewById(R.id.termsConditions);
        //go back to user profile
        findViewById(R.id.button_back2).setOnClickListener(v -> finish());

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
    }
}