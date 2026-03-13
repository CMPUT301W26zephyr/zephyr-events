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
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.util.BottomNavHelper;

import java.util.ArrayList;
import java.util.List;

public class EventsListActivity extends AppCompatActivity {

    private EventListAdapter adapter;
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> displayedEvents = new ArrayList<>();
    private EventController controller;
    private EditText etSearchBar; // Added as a class variable to keep track of searches

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_events_list);

        BottomNavHelper.setupBottomNav(this);

        controller = EventController.getInstance();

        // Setup the ListView and Adapter completely empty first
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
        etSearchBar = findViewById(R.id.etSearchBar);
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
            startActivity(new Intent(this, FilterEventsActivity.class));
        });

        // Setup Bottom Nav
        BottomNavHelper.setupBottomNav(this);

    }

    // Refresh data every time the screen becomes visible
    @Override
    protected void onResume() {
        super.onResume();

        controller.getAllEvents(new RepositoryCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> result) {
                allEvents.clear();
                if (result != null) {
                    allEvents.addAll(result);
                }

                // Re-apply the search filter if the user had text in the search bar
                String currentQuery = "";
                if (etSearchBar != null && etSearchBar.getText() != null) {
                    currentQuery = etSearchBar.getText().toString().trim();
                }
                filterEvents(currentQuery);
            }

            @Override
            public void onFailure(Exception e) {
                // Silently ignore or show a toast if network fails
            }
        });
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

        adapter.notifyDataSetChanged();
    }

    private void openEventDetail(String eventKey, boolean invited) {
        Intent intent = new Intent(this, EventDetailViewActivity.class);
        intent.putExtra(EventDetailViewActivity.EXTRA_EVENT, eventKey);
        intent.putExtra(EventDetailViewActivity.EXTRA_INVITED, invited);
        startActivity(intent);
    }
}