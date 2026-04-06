package com.example.zephyrevents.view.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.util.HomeExploreConstants;
import com.example.zephyrevents.view.adapter.EventListAdapter;
import com.example.zephyrevents.view.event.EventDetailViewActivity;
import com.example.zephyrevents.view.event.FilterEventsActivity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;


import java.util.Locale;


public class EventsListFragment extends Fragment {

    /** When set from home "See all", the list is restricted (and ordered) for that explore row. */
    public enum HomeListCategory {
        NONE,
        CLOSING_SOON,
        TRENDING,
        NEW_WITHIN_7_DAYS,
        FREE
    }

    public static final String ARG_HOME_CATEGORY = "arg_home_category";
    public static final String ARG_FOCUS_SEARCH = "arg_focus_search";

    private EventListAdapter adapter;
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> displayedEvents = new ArrayList<>();
    private EventController controller;
    private EditText etSearchBar; // Added as a class variable to keep track of searches

    private HomeListCategory homeListCategory = HomeListCategory.NONE;

    private boolean filterAnytime = true;
    private long filterRangeStartMs = -1L;
    private long filterRangeEndMs = -1L;

    private boolean filterOnlyWithSpace = false;
    private boolean focusSearch = false;

    private static final String STATE_FILTER_ANYTIME = "state_filter_anytime";
    private static final String STATE_FILTER_RANGE_START = "state_filter_range_start";
    private static final String STATE_FILTER_RANGE_END = "state_filter_range_end";
    private static final String STATE_FILTER_ONLY_SPACE = "state_filter_only_space";
    private static final String STATE_FOCUS_SEARCH = "state_focus_search";
    private static final String STATE_HOME_LIST_CATEGORY = "state_home_list_category";

    /**
     * @param category pass {@link HomeListCategory#NONE} for the full public list (e.g. View all from toolbar).
     */
    public static EventsListFragment newInstance(HomeListCategory category) {
        EventsListFragment fragment = new EventsListFragment();
        if (category != null && category != HomeListCategory.NONE) {
            Bundle args = new Bundle();
            args.putString(ARG_HOME_CATEGORY, category.name());
            fragment.setArguments(args);
        }
        return fragment;
    }

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

        if (savedInstanceState != null) {
            filterAnytime = savedInstanceState.getBoolean(STATE_FILTER_ANYTIME, true);
            filterRangeStartMs = savedInstanceState.getLong(STATE_FILTER_RANGE_START, -1L);
            filterRangeEndMs = savedInstanceState.getLong(STATE_FILTER_RANGE_END, -1L);
            filterOnlyWithSpace = savedInstanceState.getBoolean(STATE_FILTER_ONLY_SPACE, false);
            focusSearch = savedInstanceState.getBoolean(STATE_FOCUS_SEARCH, false);
            String cat = savedInstanceState.getString(STATE_HOME_LIST_CATEGORY);
            if (cat != null) {
                try {
                    homeListCategory = HomeListCategory.valueOf(cat);
                } catch (IllegalArgumentException ignored) {
                    homeListCategory = HomeListCategory.NONE;
                }
            }
        } else {
            Bundle args = getArguments();
            if (args != null) {
                focusSearch = args.getBoolean(ARG_FOCUS_SEARCH, false);
                String cat = args.getString(ARG_HOME_CATEGORY);
                if (cat != null) {
                    try {
                        homeListCategory = HomeListCategory.valueOf(cat);
                    } catch (IllegalArgumentException ignored) {
                        homeListCategory = HomeListCategory.NONE;
                    }
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_FILTER_ANYTIME, filterAnytime);
        outState.putLong(STATE_FILTER_RANGE_START, filterRangeStartMs);
        outState.putLong(STATE_FILTER_RANGE_END, filterRangeEndMs);
        outState.putBoolean(STATE_FILTER_ONLY_SPACE, filterOnlyWithSpace);
        outState.putBoolean(STATE_FOCUS_SEARCH, focusSearch);
        outState.putString(STATE_HOME_LIST_CATEGORY, homeListCategory.name());
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
        applySearchHint();
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

        if (focusSearch && etSearchBar != null) {
            etSearchBar.requestFocus();
            etSearchBar.postDelayed(() -> {
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(etSearchBar, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 100); // Delay to wait for rendered
            focusSearch = false; // Reset (so gone next time we visit hte fragment)
        }
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

    private void applySearchHint() {
        if (etSearchBar == null) {
            return;
        }
        int hintRes;
        switch (homeListCategory) {
            case CLOSING_SOON:
                hintRes = R.string.events_list_search_hint_closing_soon;
                break;
            case TRENDING:
                hintRes = R.string.events_list_search_hint_trending;
                break;
            case NEW_WITHIN_7_DAYS:
                hintRes = R.string.events_list_search_hint_new;
                break;
            case FREE:
                hintRes = R.string.events_list_search_hint_free;
                break;
            case NONE:
            default:
                hintRes = R.string.events_list_search_hint_default;
                break;
        }
        etSearchBar.setHint(hintRes);
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

        long now = System.currentTimeMillis();

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

            if (homeListCategory != HomeListCategory.NONE && !matchesHomeExploreCategory(e, now)) {
                continue;
            }

            displayedEvents.add(e);
        }

        sortForHomeCategory();
        adapter.notifyDataSetChanged();
    }

    private boolean matchesHomeExploreCategory(Event e, long now) {
        switch (homeListCategory) {
            case CLOSING_SOON: {
                long regEnd = e.getRegistrationEndTime();
                return regEnd > now && regEnd <= now + HomeExploreConstants.CLOSING_SOON_MS;
            }
            case TRENDING:
                return true;
            case NEW_WITHIN_7_DAYS: {
                long regStart = e.getRegistrationStartTime();
                if (regStart <= 0 || regStart > now) {
                    return false;
                }
                return now - regStart <= HomeExploreConstants.NEW_ON_LOTTOFY_MAX_AGE_MS;
            }
            case FREE:
                return e.getPrice() <= 0;
            case NONE:
            default:
                return true;
        }
    }

    private void sortForHomeCategory() {
        switch (homeListCategory) {
            case CLOSING_SOON:
                displayedEvents.sort(Comparator.comparingLong(Event::getRegistrationEndTime));
                break;
            case TRENDING:
                displayedEvents.sort((a, b) ->
                        Integer.compare(b.getCurrentApplicants(), a.getCurrentApplicants()));
                break;
            case NEW_WITHIN_7_DAYS:
                displayedEvents.sort((a, b) ->
                        Long.compare(b.getRegistrationStartTime(), a.getRegistrationStartTime()));
                break;
            case FREE:
                displayedEvents.sort((a, b) ->
                        Integer.compare(b.getCurrentApplicants(), a.getCurrentApplicants()));
                break;
            case NONE:
            default:
                break;
        }
    }

    private void openEventDetail(String eventKey, boolean invited) {
        Intent intent = new Intent(requireContext(), EventDetailViewActivity.class);
        intent.putExtra(EventDetailViewActivity.EXTRA_EVENT, eventKey);
        intent.putExtra(EventDetailViewActivity.EXTRA_INVITED, invited);  // TODO: Make this better (like what even is this)
        startActivity(intent);
    }
}