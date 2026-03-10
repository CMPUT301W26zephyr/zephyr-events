package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.model.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Event Detail View (CRC: EventDetailView).
 * Displays details; captures join/leave waitlist actions; displays QR code and participant map.
 * Collaborators: EventController.
 */
public class EventDetailViewActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT = "extra_event";
    /** Intent extra key: true if the user has been invited (lottery selected them). */
    public static final String EXTRA_INVITED = "extra_invited";

    private Event event;
    private boolean isInvited;
    private boolean isOnWaitlist;

    private TextView statusTag;
    private TextView eventTitle;
    private TextView eventPrice;
    private TextView eventDate;
    private TextView eventLocation;
    private TextView organizerName;
    private TextView eventAbout;
    private TextView waitlistCapacity;
    private TextView waitlistApplicants;
    private TextView waitlistRegistrationEnds;
    private View eventImageContainer;
    private Button buttonPrimary;
    private Button buttonSecondary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        event = getEventFromIntent();
        if (event == null) {
            finish();
            return;
        }
        isInvited = getIntent().getBooleanExtra(EXTRA_INVITED, false);
        EventController controller = EventController.getInstance();
        isOnWaitlist = event.getEventId() != null && controller.isOnWaitlist(event.getEventId());

        findViews();
        setupBackButton();
        bindEventToViews();
        updateButtonState();

        LinearLayout btnViewEntrants = findViewById(R.id.btn_view_entrants);
        btnViewEntrants.setOnClickListener(v -> {
            startActivity(new Intent(EventDetailViewActivity.this, OrganizerEntrantsListView.class));
        });
    }

    private Event getEventFromIntent() {
        String key = getIntent().getStringExtra(EXTRA_EVENT);
        if (key == null) return null;
        return EventController.getInstance().getEvent(key);
    }

    private void findViews() {
        statusTag = findViewById(R.id.status_tag);
        eventTitle = findViewById(R.id.event_title);
        eventPrice = findViewById(R.id.event_price);
        eventDate = findViewById(R.id.event_date);
        eventLocation = findViewById(R.id.event_location);
        organizerName = findViewById(R.id.organizer_name);
        eventAbout = findViewById(R.id.event_about);
        waitlistCapacity = findViewById(R.id.waitlist_capacity);
        waitlistApplicants = findViewById(R.id.waitlist_applicants);
        waitlistRegistrationEnds = findViewById(R.id.waitlist_registration_ends);
        eventImageContainer = findViewById(R.id.event_image_container);
        buttonPrimary = findViewById(R.id.button_primary);
        buttonSecondary = findViewById(R.id.button_secondary);
    }

    private void setupBackButton() {
        ImageButton back = findViewById(R.id.button_back);
        back.setOnClickListener(v -> finish());
    }

    private void bindEventToViews() {
        eventTitle.setText(event.getName());
        eventPrice.setText(String.valueOf(event.getPrice()));
        eventDate.setText(getString(R.string.date));
        eventLocation.setText(event.getLocation().getLocationString() != null ? event.getLocation().getLocationString() : getString(R.string.location));
        organizerName.setText(event.getOrganizerName() != null ? event.getOrganizerName() : "");
        eventAbout.setText(event.getDescription() != null ? event.getDescription() : "");

        if (event.isCapacityFull()) {
            statusTag.setText(R.string.registration_closed_full);
            statusTag.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_status_tag_orange));
        } else {
            statusTag.setText(R.string.registration_open);
            statusTag.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_status_tag));
        }

        // Could be a source of ui bugs due to locale, but I doubt it, just in case, commenting.
        waitlistCapacity.setText(String.format("%s: %d", getString(R.string.total_capacity), event.getCapacity()));
        waitlistApplicants.setText(String.format("%s: %d", getString(R.string.applicants), event.getCurrentApplicants()));
        if (event.getRegistrationEndTime() > 0) {
            waitlistRegistrationEnds.setText(String.format("%s: %s", getString(R.string.registration_ends), formatRegistrationEnd(event.getRegistrationEndTime())));
        } else {
            waitlistRegistrationEnds.setText("");
        }
        setPlaceholderBackgroundByEvent();
    }

    private void setPlaceholderBackgroundByEvent() {
        String id = event.getEventId();
        if (id != null && id.equals(EventController.KEY_PIANO)) {
            eventImageContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.event_placeholder_piano));
        } else {
            eventImageContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.event_placeholder_swimming));
        }
    }

    private String formatRegistrationEnd(long timeMillis) {
        return new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(new Date(timeMillis));
    }

    private void updateButtonState() {
        if (isInvited) {
            showAcceptDeclineButtons();
            return;
        }
        if (event.isCapacityFull() && !isOnWaitlist) {
            showCapacityFullButton();
            return;
        }
        if (isOnWaitlist) showLeaveWaitlistButton();
        else showJoinWaitlistButton();
    }

    private void showJoinWaitlistButton() {
        buttonPrimary.setVisibility(View.VISIBLE);
        buttonPrimary.setEnabled(true);
        buttonPrimary.setText(R.string.join_waitlist);
        buttonPrimary.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_filled));
        buttonPrimary.setTextColor(ContextCompat.getColor(this, R.color.white));
        buttonPrimary.setOnClickListener(v -> {
            isOnWaitlist = true;
            if (event.getEventId() != null) EventController.getInstance().addToWaitlist(event.getEventId());
            updateButtonState();
            Toast.makeText(this, R.string.join_waitlist, Toast.LENGTH_SHORT).show();
        });
        buttonSecondary.setVisibility(View.GONE);
    }

    private void showLeaveWaitlistButton() {
        buttonPrimary.setVisibility(View.VISIBLE);
        buttonPrimary.setEnabled(true);
        buttonPrimary.setText(R.string.leave_waitlist);
        buttonPrimary.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_outline));
        buttonPrimary.setBackgroundTintList(null);
        buttonPrimary.setTextColor(ContextCompat.getColor(this, R.color.primary_red));
        buttonPrimary.setOnClickListener(v -> {
            isOnWaitlist = false;
            if (event.getEventId() != null) EventController.getInstance().removeFromWaitlist(event.getEventId());
            updateButtonState();
            Toast.makeText(this, R.string.leave_waitlist, Toast.LENGTH_SHORT).show();
        });
        buttonSecondary.setVisibility(View.GONE);
    }

    private void showCapacityFullButton() {
        buttonPrimary.setVisibility(View.VISIBLE);
        buttonPrimary.setEnabled(false);
        buttonPrimary.setText(R.string.capacity_full);
        buttonPrimary.setBackgroundColor(ContextCompat.getColor(this, R.color.capacity_full_button));
        buttonPrimary.setTextColor(ContextCompat.getColor(this, R.color.black));
        buttonPrimary.setOnClickListener(null);
        buttonSecondary.setVisibility(View.GONE);
    }

    private void showAcceptDeclineButtons() {
        buttonPrimary.setVisibility(View.VISIBLE);
        buttonPrimary.setText(R.string.accept_invite);
        buttonPrimary.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_filled));
        buttonPrimary.setTextColor(ContextCompat.getColor(this, R.color.white));
        buttonPrimary.setOnClickListener(v -> openYoureInScreen());
        buttonSecondary.setVisibility(View.VISIBLE);
        buttonSecondary.setText(R.string.decline_invite);
        buttonSecondary.setOnClickListener(v -> openInviteDeclinedScreen());
    }

    private void openYoureInScreen() {
        Intent intent = new Intent(this, YoureInActivity.class);
        intent.putExtra(YoureInActivity.EXTRA_EVENT_NAME, event.getName());
        startActivity(intent);
        finish();
    }

    private void openInviteDeclinedScreen() {
        Intent intent = new Intent(this, InviteDeclinedActivity.class);
        intent.putExtra(InviteDeclinedActivity.EXTRA_EVENT_NAME, event.getName());
        intent.putExtra(InviteDeclinedActivity.EXTRA_EVENT_KEY, event.getEventId());
        startActivity(intent);
        finish();
    }
}
