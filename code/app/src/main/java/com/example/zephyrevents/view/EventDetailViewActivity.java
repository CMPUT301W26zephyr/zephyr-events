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
import com.example.zephyrevents.controller.UserController;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.model.User;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.repository.UserRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventDetailViewActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT = "extra_event";
    public static final String EXTRA_INVITED = "extra_invited";

    private Event event;
    private boolean isInvited;
    private boolean isOnWaitlist;
    private String currentUserId; // Added to track who is clicking

    private UserController userController;
    private UserRepository userRepository;

    private TextView statusTag, eventTitle, eventPrice, eventDate, eventLocation;
    private TextView organizerName, eventAbout, waitlistCapacity, waitlistApplicants, waitlistRegistrationEnds;
    private View eventImageContainer;

    private View attendeeButtonsContainer;
    private View organizerDashboardContainer;

    private Button buttonPrimary, buttonSecondary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        userController = new UserController(this);
        userRepository = new UserRepository();

        // Fetch the logged-in user
        currentUserId = userController.getCurrentUserId();
        if (currentUserId == null) currentUserId = "unknown_user"; // Fallback safeguard

        findViews();
        setupBackButton();

        LinearLayout btnViewEntrants = findViewById(R.id.btn_view_entrants);
        if (btnViewEntrants != null) btnViewEntrants.setOnClickListener(v -> startActivity(new Intent(this, OrganizerEntrantsListView.class)));

        LinearLayout btnEditEvent = findViewById(R.id.btn_edit_event);
        if (btnEditEvent != null) btnEditEvent.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrganizerEventAddEditView.class);
            intent.putExtra("EXTRA_EDIT_EVENT_ID", event.getEventId());
            startActivity(intent);
            finish();
        });

        String eventId = getIntent().getStringExtra(EXTRA_EVENT);
        isInvited = getIntent().getBooleanExtra(EXTRA_INVITED, false);

        if (eventId == null) {
            Toast.makeText(this, "Error: No Event ID provided.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        EventController.getInstance().getEventById(eventId, new RepositoryCallback<Event>() {
            @Override
            public void onSuccess(Event result) {
                event = result;
                if (event != null) {
                    // Pass the user ID to the check!
                    isOnWaitlist = EventController.getInstance().isOnWaitlist(event.getEventId(), currentUserId);
                    populateUI();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventDetailViewActivity.this, "Failed to load event.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
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

        attendeeButtonsContainer = findViewById(R.id.event_detail_buttons);
        organizerDashboardContainer = findViewById(R.id.organizer_dashboard_container);

        buttonPrimary = findViewById(R.id.button_primary);
        buttonSecondary = findViewById(R.id.button_secondary);
    }

    private void setupBackButton() {
        ImageButton back = findViewById(R.id.button_back);
        if (back != null) back.setOnClickListener(v -> finish());
    }

    private void populateUI() {
        if (event == null) return;

        eventTitle.setText(event.getName() != null ? event.getName() : "Unnamed Event");
        eventPrice.setText(String.format(Locale.getDefault(), "$%.2f", event.getPrice()));

        if (event.getTime() != null && event.getTime().getStartTime() > 0) {
            eventDate.setText(formatDate(event.getTime().getStartTime()));
        } else {
            eventDate.setText(getString(R.string.date));
        }

        if (event.getLocation() != null && event.getLocation().getLocationString() != null) {
            eventLocation.setText(event.getLocation().getLocationString());
        } else {
            eventLocation.setText(R.string.location);
        }

        eventAbout.setText(event.getDescription() != null ? event.getDescription() : "No description provided.");

        boolean isCapacityFull = event.getCapacity() > 0 && event.getCurrentApplicants() >= event.getCapacity();

        if (isCapacityFull) {
            statusTag.setText(R.string.registration_closed_full);
            statusTag.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_status_tag_orange));
        } else {
            statusTag.setText(R.string.registration_open);
            statusTag.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_status_tag));
        }

        waitlistCapacity.setText(String.format(Locale.getDefault(), "%s: %d", getString(R.string.total_capacity), event.getCapacity()));
        waitlistApplicants.setText(String.format(Locale.getDefault(), "%s: %d", getString(R.string.applicants), event.getCurrentApplicants()));

        if (event.getRegistrationEndTime() > 0) {
            waitlistRegistrationEnds.setText(String.format("%s: %s", getString(R.string.registration_ends), formatDate(event.getRegistrationEndTime())));
        } else {
            waitlistRegistrationEnds.setText("");
        }

        eventImageContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.event_placeholder_swimming));

        boolean isOrganizer = currentUserId != null && currentUserId.equals(event.getOrganizerId());

        if (isOrganizer) {
            organizerName.setText("You");
            attendeeButtonsContainer.setVisibility(View.GONE);
            organizerDashboardContainer.setVisibility(View.VISIBLE);
        } else {
            organizerDashboardContainer.setVisibility(View.GONE);
            attendeeButtonsContainer.setVisibility(View.VISIBLE);

            if (event.getOrganizerId() != null) {
                userRepository.getUserById(event.getOrganizerId(), new RepositoryCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        if (user != null && user.getName() != null) {
                            organizerName.setText(user.getName());
                        } else {
                            organizerName.setText("Unknown Organizer");
                        }
                    }
                    @Override
                    public void onFailure(Exception e) {
                        organizerName.setText("Unknown Organizer");
                    }
                });
            } else {
                organizerName.setText("Unknown Organizer");
            }

            updateButtonState(isCapacityFull);
        }
    }

    private String formatDate(long timeMillis) {
        return new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(new Date(timeMillis));
    }

    private void updateButtonState(boolean isCapacityFull) {
        if (isInvited) {
            showAcceptDeclineButtons();
            return;
        }
        if (isCapacityFull && !isOnWaitlist) {
            showCapacityFullButton();
            return;
        }
        if (isOnWaitlist) {
            showLeaveWaitlistButton();
        } else {
            showJoinWaitlistButton();
        }
    }

    private void showJoinWaitlistButton() {
        buttonPrimary.setVisibility(View.VISIBLE);
        buttonPrimary.setEnabled(true);
        buttonPrimary.setText(R.string.join_waitlist);
        buttonPrimary.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_filled));
        buttonPrimary.setTextColor(ContextCompat.getColor(this, R.color.white));
        buttonPrimary.setOnClickListener(v -> {
            isOnWaitlist = true;
            if (event.getEventId() != null) {
                // PASS THE USER ID HERE!
                EventController.getInstance().addToWaitlist(event.getEventId(), currentUserId);
            }
            event.setCurrentApplicants(event.getCurrentApplicants() + 1);
            populateUI();
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
            if (event.getEventId() != null) {
                // PASS THE USER ID HERE!
                EventController.getInstance().removeFromWaitlist(event.getEventId(), currentUserId);
            }
            event.setCurrentApplicants(Math.max(0, event.getCurrentApplicants() - 1));
            populateUI();
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
        Intent intent = new Intent(this, EventStatusActivity.class);
        intent.putExtra(EventStatusActivity.EXTRA_EVENT_NAME, event.getName());
        intent.putExtra(EventStatusActivity.EXTRA_STATUS_TYPE, EventStatusActivity.STATUS_ACCEPTED);
        startActivity(intent);
        finish();
    }

    private void openInviteDeclinedScreen() {
        Intent intent = new Intent(this, EventStatusActivity.class);
        intent.putExtra(EventStatusActivity.EXTRA_EVENT_NAME, event.getName());
        intent.putExtra(EventStatusActivity.EXTRA_EVENT_KEY, event.getEventId());
        intent.putExtra(EventStatusActivity.EXTRA_STATUS_TYPE, EventStatusActivity.STATUS_DECLINED);
        startActivity(intent);
        finish();
    }
}