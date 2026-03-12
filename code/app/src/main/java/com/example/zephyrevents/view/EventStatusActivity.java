package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;

/**
 * Unified screen for displaying Acceptance or Declination of an event invite.
 */
public class EventStatusActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_NAME = "extra_event_name";
    public static final String EXTRA_EVENT_KEY = "extra_event_key";
    public static final String EXTRA_STATUS_TYPE = "extra_status_type";

    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_DECLINED = "DECLINED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_status);

        String eventName = getIntent().getStringExtra(EXTRA_EVENT_NAME);
        if (eventName == null) eventName = "";

        String eventKey = getIntent().getStringExtra(EXTRA_EVENT_KEY);
        String statusType = getIntent().getStringExtra(EXTRA_STATUS_TYPE);

        ImageView statusIcon = findViewById(R.id.status_icon);
        TextView statusTitle = findViewById(R.id.status_title);
        TextView statusMessage = findViewById(R.id.status_message);

        // Dynamically build the UI based on the status type
        if (STATUS_DECLINED.equals(statusType)) {
            // Setup Declined UI
            statusIcon.setImageResource(R.drawable.ic_cancel_circle);
            statusIcon.setColorFilter(ContextCompat.getColor(this, R.color.invite_declined_red)); // Use your red color
            statusTitle.setText(R.string.invite_declined_title);
            statusMessage.setText(getString(R.string.invite_declined_message, eventName));

            // Perform backend update for declined
            if (eventKey != null) {
                EventController.getInstance().addDeclinedEvent(eventKey);
            }
        } else {
            // Setup Accepted UI (Default)
            statusIcon.setImageResource(R.drawable.ic_check_circle);
            statusIcon.setColorFilter(ContextCompat.getColor(this, R.color.youre_in_green)); // Use your green color
            statusTitle.setText(R.string.youre_in_title);
            statusMessage.setText(getString(R.string.youre_in_message, eventName));
        }

        // Setup Buttons
        findViewById(R.id.button_back).setOnClickListener(v -> finish());

        findViewById(R.id.button_view_events).setOnClickListener(v -> {
            startActivity(new Intent(this, MyEventsActivity.class));
            finish();
        });
    }
}