package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class EventsListFragment extends Fragment {

    private EventListAdapter adapter;
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> displayedEvents = new ArrayList<>();
    private EventController controller;
    private EditText etSearchBar; // Added as a class variable to keep track of searches

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_events_list, container, false); // Rename your layout files if you want
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        controller = EventController.getInstance();

        // Setup the ListView and Adapter completely empty first
        adapter = new EventListAdapter(requireContext(), displayedEvents);
        ListView listView = view.findViewById(R.id.event_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, v, position, id) -> {
            Event event = (Event) parent.getItemAtPosition(position);
            if (event != null && event.getEventId() != null) {
                boolean invited = controller.isInvitedEvent(event.getEventId());
                openEventDetail(event.getEventId(), invited);
            }
        });

        // Setup Search Bar Filtering
        etSearchBar = view.findViewById(R.id.etSearchBar);
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
        view.findViewById(R.id.toolbar_back).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        view.findViewById(R.id.btnSearchFilter).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), FilterEventsActivity.class));
        });
    }

    // Refresh data every time the screen becomes visible
    @Override
    public void onResume() {
        super.onResume();

        controller.getAllEvents(new RepositoryCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> result) {
                allEvents.clear();
                if (result != null) {
                    for (Event e : result) {
                        if (e != null && !e.isPrivateEvent()) {
                            allEvents.add(e);
                        }
                    }
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
        Intent intent = new Intent(requireContext(), EventDetailViewActivity.class);
        intent.putExtra(EventDetailViewActivity.EXTRA_EVENT, eventKey);
        intent.putExtra(EventDetailViewActivity.EXTRA_INVITED, invited);  // TODO: Make this better (like what even is this)
        startActivity(intent);
    }
}