package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.util.BottomNavHelper;

import java.util.ArrayList;
import java.util.List;

public class EventsListActivity extends AppCompatActivity {

    private EventListAdapter adapter;
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> displayedEvents = new ArrayList<>();
    private EventController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_events_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // 1. Apply top padding ONLY to the toolbar so it stretches behind the status bar
            View toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                // Keep its existing horizontal/bottom padding, just add the system top inset
                toolbar.setPadding(toolbar.getPaddingLeft(), systemBars.top, toolbar.getPaddingRight(), toolbar.getPaddingBottom());
            }

            // 2. Apply bottom padding ONLY to the bottom nav so it stretches behind the gesture/nav bar
            View bottomNav = findViewById(R.id.bottom_nav);
            if (bottomNav != null) {
                bottomNav.setPadding(bottomNav.getPaddingLeft(), bottomNav.getPaddingTop(), bottomNav.getPaddingRight(), systemBars.bottom);
            }

            // Note: We completely removed `v.setPadding(...)` here so the root layout stops shrinking!
            return insets;
        });

        // Initialize Data
        controller = EventController.getInstance();
        allEvents = controller.getEventsForList();

        // We use a separate list for the adapter so we can filter it without losing the original data
        displayedEvents.addAll(allEvents);

        // Setup ListView & Adapter
        adapter = new EventListAdapter(this, displayedEvents);
        ListView listView = findViewById(R.id.event_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Event event = (Event) parent.getItemAtPosition(position);
            if (event != null && event.getEventId() != null) {
                boolean invited = controller.isInvitedEvent(event.getEventId());
                openEventDetail(event.getEventId(), invited);
            }
        });

        // Setup Search Bar Filtering
        EditText etSearchBar = findViewById(R.id.etSearchBar);
        etSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup Toolbar Buttons
        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());

        findViewById(R.id.btnSearchFilter).setOnClickListener(v -> {
            // Keep the filter activity functionality from the old search screen
            startActivity(new Intent(this, FilterEventsActivity.class));
        });

        // Setup Bottom Nav
        BottomNavHelper.setupBottomNav(this);
    }

    /**
     * Filters the master list based on the search query and updates the adapter.
     */
    private void filterEvents(String query) {
        displayedEvents.clear();

        if (query.isEmpty()) {
            displayedEvents.addAll(allEvents);
        } else {
            String lower = query.toLowerCase();
            for (Event e : allEvents) {
                if ((e.getName() != null && e.getName().toLowerCase().contains(lower)) ||
                        (e.getDescription() != null && e.getDescription().toLowerCase().contains(lower))) {
                    displayedEvents.add(e);
                }
            }
        }

        // Tells the ListView to refresh itself with the newly filtered data
        adapter.notifyDataSetChanged();
    }

    private void openEventDetail(String eventKey, boolean invited) {
        Intent intent = new Intent(this, EventDetailViewActivity.class);
        intent.putExtra(EventDetailViewActivity.EXTRA_EVENT, eventKey);
        intent.putExtra(EventDetailViewActivity.EXTRA_INVITED, invited);
        startActivity(intent);
    }
}