package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class AdminBrowseEventActivity extends AppCompatActivity {

    private ListView listView;
    private AdminGenericEventAdapter adapter;

    // Strongly typed event data
    private List<Event> eventList;          // full list
    private List<Event> displayedEvents;    // filtered list

    // Adapter-facing list kept as Object for compatibility
    private List<Object> adapterItems;

    private EditText etSearchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_browse_event);

        findViewById(R.id.button_back2).setOnClickListener(v -> finish());

        listView = findViewById(R.id.event_list);
        etSearchBar = findViewById(R.id.etSearchBar);

        eventList = new ArrayList<>();
        displayedEvents = new ArrayList<>();
        adapterItems = new ArrayList<>();

        adapter = new AdminGenericEventAdapter(
                this,
                adapterItems,
                R.layout.admin_event_card
        );

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < 0 || position >= displayedEvents.size()) return;

            Event selectedEvent = displayedEvents.get(position);

            Intent intent = new Intent(this, EventDetailViewActivity.class);
            intent.putExtra(EventDetailViewActivity.EXTRA_EVENT, selectedEvent.getEventId());
            intent.putExtra("isAdminView", true);
            startActivity(intent);
        });

        if (etSearchBar != null) {
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
        }

        setupBottomNav();
        loadEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }

    private void setupBottomNav() {

        findViewById(R.id.nav_home).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("TARGET_TAB", "Home");
            startActivity(intent);
        });

        findViewById(R.id.nav_my_events).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("TARGET_TAB", "MyEvents");
            startActivity(intent);
        });

        findViewById(R.id.nav_create_event).setOnClickListener(v -> {
            startActivity(new Intent(this, OrganizerEventAddEditView.class));
        });

        findViewById(R.id.nav_scan_qr).setOnClickListener(v -> {
            startActivity(new Intent(this, QrScannerActivity.class));
        });
    }

    private void loadEvents() {
        EventController.getInstance().getAllEvents(new RepositoryCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> result) {
                runOnUiThread(() -> {
                    eventList.clear();

                    if (result != null) {
                        eventList.addAll(result);
                    }

                    String currentQuery = "";
                    if (etSearchBar != null && etSearchBar.getText() != null) {
                        currentQuery = etSearchBar.getText().toString().trim();
                    }

                    filterEvents(currentQuery);
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(
                                AdminBrowseEventActivity.this,
                                "Failed to load events",
                                Toast.LENGTH_SHORT
                        ).show()
                );
            }
        });
    }

    private void filterEvents(String query) {
        displayedEvents.clear();
        adapterItems.clear();

        if (query == null || query.isEmpty()) {
            displayedEvents.addAll(eventList);
        } else {
            String lower = query.toLowerCase();

            for (Event event : eventList) {
                String name = event.getName();
                String description = event.getDescription();

                boolean matchesName = name != null && name.toLowerCase().contains(lower);
                boolean matchesDescription = description != null && description.toLowerCase().contains(lower);

                if (matchesName || matchesDescription) {
                    displayedEvents.add(event);
                }
            }
        }

        adapterItems.addAll(displayedEvents);
        adapter.notifyDataSetChanged();
    }
}