package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.controller.UserController;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.model.EventTime;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.util.BottomNavHelper;
import com.example.zephyrevents.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * HomeActivity is the default first screen after the authentication (log in) step
 * Shows large cards of featured events (e.g. by popularity, proximity)
 * Checks if User account still exists upon creation; returns to WelcomeActivity if not exist.
 */
public class HomeActivity extends AppCompatActivity {
    UserController userController;
    private FeaturedEventListAdapter adapter;
    private List<Event> featuredEvents = new ArrayList<>();
    private EventController eventController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavHelper.setupBottomNav(this);

        eventController = EventController.getInstance();

        // Initially the ListView and Adapter are EMPTY (to add later)
        adapter = new FeaturedEventListAdapter(this, featuredEvents);
        ListView listView = findViewById(R.id.event_list);
        listView.setAdapter(adapter);

        // ListView Click Listener to open Event Details
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Event event = (Event) parent.getItemAtPosition(position);
            if (event != null && event.getEventId() != null) {
                boolean invited = eventController.isInvitedEvent(event.getEventId());
                openEventDetail(event.getEventId(), invited);
            }
        });

        TextView tvViewAll = findViewById(R.id.view_all);
        if (tvViewAll != null) {
            tvViewAll.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, EventsListActivity.class));
            });
        }

        // Search icon does the same thing as the textView for now
        // TODO: Possible to make it open the search box? Via intent
        ImageView searchIcon = findViewById(R.id.btn_search);
        if (searchIcon != null) {
            searchIcon.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, EventsListActivity.class));
            });
        }

        // Fetch user data in the background to verify that account still exists
        userController = new UserController(this);
        userController.fetchCurrentUser(new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User result) {
                // User exists! Can do Personalized UI updates here (e.g. Welcome, <Name>!)
            }

            @Override
            public void onFailure(Exception e) {
                // Check if the controller wiped the session because the doc was missing
                if (!userController.isUserLoggedIn()) {
                    // Kick them back to WelcomeActivity
                    Intent intent = new Intent(HomeActivity.this, WelcomeActivity.class);
                    intent.putExtra("TOAST_MESSAGE", "Your Account Has Been Removed.");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // If it was just a normal network error (e.g., user is offline), do nothing (or something, idrk)
                }
            }
        });
    }

    // Refresh featured events every time the screen becomes visible
    @Override
    protected void onResume() {
        super.onResume();
        loadFeaturedEvents();
    }

    /**
     * Loads featured events
     * NOTE: Placeholder logic currently just picks three random events.
     */
    private void loadFeaturedEvents() {
        eventController.getAllEvents(new RepositoryCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> result) {
                featuredEvents.clear();

                if (result != null && !result.isEmpty()) {
                    // Create a copy of the list, shuffle, select up to 3 events
                    List<Event> shuffledList = new ArrayList<>(result);
                    Collections.shuffle(shuffledList);
                    int limit = Math.min(3, shuffledList.size());
                    for (int i = 0; i < limit; i++) {
                        featuredEvents.add(shuffledList.get(i));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(HomeActivity.this, "Failed to load featured events.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openEventDetail(String eventKey, boolean invited) {
        Intent intent = new Intent(this, EventDetailViewActivity.class);
        intent.putExtra(EventDetailViewActivity.EXTRA_EVENT, eventKey);
        intent.putExtra(EventDetailViewActivity.EXTRA_INVITED, invited);
        startActivity(intent);
    }
}