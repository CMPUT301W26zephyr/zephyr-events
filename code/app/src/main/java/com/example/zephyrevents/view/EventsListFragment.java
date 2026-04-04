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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.repository.RepositoryCallback;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;


import java.util.Locale;


public class EventsListFragment extends Fragment {

    private EventListAdapter adapter;
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> displayedEvents = new ArrayList<>();
    private EventController controller;
    private EditText etSearchBar; // Added as a class variable to keep track of searches

    private boolean filterAnytime = true;
    private long filterRangeStartMs = -1L;
    private long filterRangeEndMs = -1L;

    private boolean filterOnlyWithSpace = false;

    private static final String STATE_FILTER_ANYTIME = "state_filter_anytime";
    private static final String STATE_FILTER_RANGE_START = "state_filter_range_start";
    private static final String STATE_FILTER_RANGE_END = "state_filter_range_end";
    private static final String STATE_FILTER_ONLY_SPACE = "state_filter_only_space";

    private final ActivityResultLauncher<Intent> filterLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if(result.getResultCode() != Activity.RESULT_OK || result.getData() == null){
                    return;
                }

                Intent data = result.getData();
                filterAnytime = data.getBooleanExtra(FilterEventsActivity.EXTRA_ANYTIME,true);
                filterRangeStartMs = data.getLongExtra(FilterEventsActivity.EXTRA_RANGE_START_MS,-1L);
                filterRangeEndMs = data.getLongExtra(FilterEventsActivity.EXTRA_RANGE_END_MS,-1L);
                filterOnlyWithSpace = data.getBooleanExtra(FilterEventsActivity.EXTRA_ONLY_WITH_SPACE,false);
                applyFilters();


            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null){
            filterAnytime = savedInstanceState.getBoolean(STATE_FILTER_ANYTIME, true);
            filterRangeStartMs = savedInstanceState.getLong(STATE_FILTER_RANGE_START, -1L);
            filterRangeEndMs = savedInstanceState.getLong(STATE_FILTER_RANGE_END, -1L);
            filterOnlyWithSpace = savedInstanceState.getBoolean(STATE_FILTER_ONLY_SPACE, false);

        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_FILTER_ANYTIME, filterAnytime);
        outState.putLong(STATE_FILTER_RANGE_START, filterRangeStartMs);
        outState.putLong(STATE_FILTER_RANGE_END, filterRangeEndMs);
        outState.putBoolean(STATE_FILTER_ONLY_SPACE, filterOnlyWithSpace);




    }

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
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup Toolbar Buttons
        view.findViewById(R.id.toolbar_back).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        view.findViewById(R.id.btnSearchFilter).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), FilterEventsActivity.class);
            intent.putExtra(FilterEventsActivity.EXTRA_ANYTIME, filterAnytime);
            intent.putExtra(FilterEventsActivity.EXTRA_RANGE_START_MS, filterRangeStartMs);
            intent.putExtra(FilterEventsActivity.EXTRA_RANGE_END_MS, filterRangeEndMs);
            intent.putExtra(FilterEventsActivity.EXTRA_ONLY_WITH_SPACE, filterOnlyWithSpace);
            filterLauncher.launch(intent);
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


                applyFilters();
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
    private void applyFilters() {
        displayedEvents.clear();

        String query = "";

        if(etSearchBar != null && etSearchBar.getText() != null){
            query = etSearchBar.getText().toString().trim().toLowerCase(Locale.getDefault());
        }

        for (Event e : allEvents){
            if(e == null){
                continue;
            }

            if(!query.isEmpty()){
                String n = e.getName() != null ? e.getName().toLowerCase(Locale.getDefault()) : "";
                String d = e.getDescription() != null ? e.getDescription().toLowerCase(Locale.getDefault()) : "";
                if (!n.contains(query) && !d.contains(query)){
                    continue;
                }

            }

            if(!filterAnytime){
                if (filterRangeStartMs < 0 || filterRangeEndMs < 0){
                    continue;
                }

                if(e.getTime() == null){
                    continue;
                }
                long es = e.getTime().getStartTime();
                long ee = e.getTime().getEndTime();

                if(!(es < filterRangeEndMs && ee > filterRangeStartMs)){
                    continue;
                }
            }

            if(filterOnlyWithSpace){
                Integer cap = e.getWaitlistCapacity();
                if(cap != null && cap > 0 && e.getCurrentApplicants() >= cap){
                    continue;
                }
            }
            displayedEvents.add(e);
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