package com.example.zephyrevents.view;

import android.content.Intent;
import android.net.Uri;
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
import com.example.zephyrevents.model.Status;
import com.example.zephyrevents.model.User;
import com.example.zephyrevents.model.WaitlistEntry;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.repository.UserRepository;
import com.example.zephyrevents.repository.WaitlistRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventDetailViewActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT = "extra_event";
    public static final String EXTRA_INVITED = "extra_invited";

    private Event event;
    private boolean isInvited;

    private String currentUserId;
    private UserController userController;
    private UserRepository userRepository;

    private TextView statusTag, eventTitle, eventPrice, eventDate, eventLocation;
    private TextView organizerName, eventAbout, totalCapacity, waitlistCapacity, waitlistApplicants, waitlistRegistrationEnds;
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

        currentUserId = userController.getCurrentUserId();
        if (currentUserId == null) currentUserId = "unknown_user";

        findViews();
        setupBackButton();

        String eventId = getIntent().getStringExtra(EXTRA_EVENT);
        isInvited = getIntent().getBooleanExtra(EXTRA_INVITED, false);

        // Handle link parameter (e.g. from qr code)
        if (eventId == null) {
            Uri data = getIntent().getData();
            if (data != null) {
                eventId = data.getQueryParameter("id");
            }
        }

        if (eventId == null) {
            Toast.makeText(this, "Error: No Event ID provided.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup Organizer Dashboard buttons
        LinearLayout btnViewEntrants = findViewById(R.id.btn_view_entrants);
        if (btnViewEntrants != null) {
            btnViewEntrants.setOnClickListener(v -> {
                Intent intent = new Intent(this, OrganizerEntrantsListView.class);
                intent.putExtra(EXTRA_EVENT, event.getEventId());
                startActivity(intent);
            });
        }

        LinearLayout btnEditEvent = findViewById(R.id.btn_edit_event);
        if (btnEditEvent != null) {
            btnEditEvent.setOnClickListener(v -> {
                Intent intent = new Intent(this, OrganizerEventAddEditView.class);
                intent.putExtra("EXTRA_EDIT_EVENT_ID", event.getEventId());
                startActivity(intent);
                finish();
            });
        }

        EventController.getInstance().getEventById(eventId, new RepositoryCallback<Event>() {
            @Override
            public void onSuccess(Event result) {
                event = result;
                if (event != null) {
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
        totalCapacity = findViewById(R.id.total_capacity);
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
            eventDate.setText(new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(new Date(event.getTime().getStartTime())));
        } else {
            eventDate.setText(getString(R.string.date));
        }

        if (event.getLocation() != null && event.getLocation().getLocationString() != null) {
            eventLocation.setText(event.getLocation().getLocationString());
        } else {
            eventLocation.setText(R.string.location);
        }

        eventAbout.setText(event.getDescription() != null ? event.getDescription() : "No description provided.");

        if (totalCapacity != null) {
            totalCapacity.setText(String.valueOf(event.getCapacity()));
        }

        String limitStr = (event.getWaitlistCapacity() != null && event.getWaitlistCapacity() > 0)
                ? String.valueOf(event.getWaitlistCapacity())
                : "Unlimited";
        if (waitlistCapacity != null) {
            waitlistCapacity.setText(limitStr);
        }

        if (event.getRegistrationEndTime() > 0) {
            waitlistRegistrationEnds.setText(new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(new Date(event.getRegistrationEndTime())));
        } else {
            waitlistRegistrationEnds.setText("N/A");
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
                        organizerName.setText(user != null && user.getName() != null ? user.getName() : "Unknown Organizer");
                    }
                    @Override
                    public void onFailure(Exception e) { organizerName.setText("Unknown Organizer"); }
                });
            } else {
                organizerName.setText("Unknown Organizer");
            }
        }

        // one database call to fetch the waitlist, calculate counts, check lottery status, and check user status
        new WaitlistRepository().getWaitlist(event.getEventId(), new RepositoryCallback<List<WaitlistEntry>>() {
            @Override
            public void onSuccess(List<WaitlistEntry> entries) {
                int trueCount = (entries != null) ? entries.size() : 0;
                waitlistApplicants.setText(String.valueOf(trueCount));

                boolean lotteryRun = false;
                WaitlistEntry myEntry = null;

                // Scan the list for lottery status and our own entry
                if (entries != null) {
                    for (WaitlistEntry e : entries) {
                        if (e.getStatus() != Status.WAITLISTED) {
                            lotteryRun = true; // If anyone has a post-lottery status, the lottery has run
                        }
                        if (e.getUserId() != null && e.getUserId().equals(currentUserId)) {
                            myEntry = e;
                        }
                    }
                }

                boolean trueCapacityFull = event.getWaitlistCapacity() != null && event.getWaitlistCapacity() > 0 && trueCount >= event.getWaitlistCapacity();
                boolean pastDeadline = event.getRegistrationEndTime() > 0 && System.currentTimeMillis() > event.getRegistrationEndTime();

                // Waitlist is closed for NEW joins if ANY of these three things are true
                boolean isClosedForNew = trueCapacityFull || lotteryRun || pastDeadline;

                if (isClosedForNew) {
                    statusTag.setText("CLOSED");
                    statusTag.setBackground(ContextCompat.getDrawable(EventDetailViewActivity.this, R.drawable.bg_status_tag_orange));
                } else {
                    statusTag.setText(R.string.registration_open);
                    statusTag.setBackground(ContextCompat.getDrawable(EventDetailViewActivity.this, R.drawable.bg_status_tag));
                }

                // If not organizer, configure the attendee buttons
                if (!isOrganizer) {
                    if (myEntry == null) {
                        // User is NOT on the list. Can they join?
                        if (isClosedForNew) {
                            showWaitlistClosedButton(trueCapacityFull, lotteryRun, pastDeadline);
                        } else {
                            showJoinWaitlistButton(new WaitlistRepository());
                        }
                    } else {
                        // User IS on the list. Handle buttons based on their status.
                        switch (myEntry.getStatus()) {
                            case WAITLISTED:
                                showLeaveWaitlistButton(new WaitlistRepository());
                                break;
                            case SELECTED:
                                showAcceptDeclineButtons(new WaitlistRepository());
                                break;
                            case LOST:
                                showLostButton();
                                break;
                            case ACCEPTED:
                                buttonPrimary.setVisibility(View.VISIBLE);
                                buttonPrimary.setEnabled(false);
                                buttonPrimary.setText("STATUS: CONFIRMED");
                                buttonPrimary.setBackgroundColor(ContextCompat.getColor(EventDetailViewActivity.this, R.color.youre_in_green));
                                buttonSecondary.setVisibility(View.GONE);
                                break;
                            case DECLINED:
                                buttonPrimary.setVisibility(View.VISIBLE);
                                buttonPrimary.setEnabled(false);
                                buttonPrimary.setText("STATUS: DECLINED");
                                buttonPrimary.setBackgroundColor(ContextCompat.getColor(EventDetailViewActivity.this, R.color.invite_declined_red));
                                buttonSecondary.setVisibility(View.GONE);
                                break;
                        }
                    }
                }
            }
            @Override
            public void onFailure(Exception e) {
                waitlistApplicants.setText(String.valueOf(event.getCurrentApplicants()));
            }
        });
    }

    private void showJoinWaitlistButton(WaitlistRepository repo) {
        buttonPrimary.setVisibility(View.VISIBLE);
        buttonPrimary.setEnabled(true);
        buttonPrimary.setText(R.string.join_waitlist);
        buttonPrimary.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_filled));
        buttonPrimary.setTextColor(ContextCompat.getColor(this, R.color.white));
        buttonPrimary.setOnClickListener(v -> {
            WaitlistEntry newEntry = new WaitlistEntry(currentUserId, event.getEventId(), 0.0, 0.0, Status.WAITLISTED);
            repo.addUserToWaitlist(newEntry, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Toast.makeText(EventDetailViewActivity.this, "Joined Waitlist!", Toast.LENGTH_SHORT).show();
                    populateUI();
                }
                @Override
                public void onFailure(Exception e) {}
            });
        });
        buttonSecondary.setVisibility(View.GONE);
    }

    private void showLeaveWaitlistButton(WaitlistRepository repo) {
        buttonPrimary.setVisibility(View.VISIBLE);
        buttonPrimary.setEnabled(true);
        buttonPrimary.setText(R.string.leave_waitlist);
        buttonPrimary.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_outline));
        buttonPrimary.setBackgroundTintList(null);
        buttonPrimary.setTextColor(ContextCompat.getColor(this, R.color.primary_red));
        buttonPrimary.setOnClickListener(v -> {
            repo.removeUserFromWaitlist(event.getEventId(), currentUserId, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Toast.makeText(EventDetailViewActivity.this, "Left Waitlist", Toast.LENGTH_SHORT).show();
                    populateUI();
                }
                @Override
                public void onFailure(Exception e) {}
            });
        });
        buttonSecondary.setVisibility(View.GONE);
    }

    private void showAcceptDeclineButtons(WaitlistRepository repo) {
        buttonPrimary.setVisibility(View.VISIBLE);
        buttonPrimary.setEnabled(true);
        buttonPrimary.setText(R.string.accept_invite);
        buttonPrimary.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_filled));
        buttonPrimary.setTextColor(ContextCompat.getColor(this, R.color.white));
        buttonPrimary.setOnClickListener(v -> {
            repo.updateStatus(event.getEventId(), currentUserId, Status.ACCEPTED, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Intent intent = new Intent(EventDetailViewActivity.this, EventStatusActivity.class);
                    intent.putExtra(EventStatusActivity.EXTRA_EVENT_NAME, event.getName());
                    intent.putExtra(EventStatusActivity.EXTRA_STATUS_TYPE, EventStatusActivity.STATUS_ACCEPTED);
                    startActivity(intent);
                    finish();
                }
                @Override
                public void onFailure(Exception e) {}
            });
        });

        buttonSecondary.setVisibility(View.VISIBLE);
        buttonSecondary.setText(R.string.decline_invite);
        buttonSecondary.setOnClickListener(v -> {
            repo.updateStatus(event.getEventId(), currentUserId, Status.DECLINED, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Intent intent = new Intent(EventDetailViewActivity.this, EventStatusActivity.class);
                    intent.putExtra(EventStatusActivity.EXTRA_EVENT_NAME, event.getName());
                    intent.putExtra(EventStatusActivity.EXTRA_EVENT_KEY, event.getEventId());
                    intent.putExtra(EventStatusActivity.EXTRA_STATUS_TYPE, EventStatusActivity.STATUS_DECLINED);
                    startActivity(intent);
                    finish();
                }
                @Override
                public void onFailure(Exception e) {}
            });
        });
    }

    private void showWaitlistClosedButton(boolean capacity, boolean lottery, boolean deadline) {
        buttonPrimary.setVisibility(View.VISIBLE);
        buttonPrimary.setEnabled(false);

        // Tell the user exactly why the button is grayed out
        if (lottery) {
            buttonPrimary.setText("LOTTERY COMPLETE");
        } else if (deadline) {
            buttonPrimary.setText("REGISTRATION CLOSED");
        } else {
            buttonPrimary.setText(R.string.capacity_full);
        }

        // Gray it out
        buttonPrimary.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        buttonPrimary.setTextColor(ContextCompat.getColor(this, R.color.white));
        buttonPrimary.setOnClickListener(null);
        buttonSecondary.setVisibility(View.GONE);
    }

    private void showLostButton() {
        buttonPrimary.setVisibility(View.VISIBLE);
        buttonPrimary.setEnabled(false);
        buttonPrimary.setText("STATUS: NOT SELECTED");
        buttonPrimary.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        buttonPrimary.setTextColor(ContextCompat.getColor(this, R.color.white));
        buttonPrimary.setOnClickListener(null);
        buttonSecondary.setVisibility(View.GONE);
    }
}