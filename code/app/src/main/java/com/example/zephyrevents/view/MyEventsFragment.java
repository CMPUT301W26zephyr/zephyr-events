package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.model.WaitlistEntry;

import com.example.zephyrevents.controller.UserController;
import com.example.zephyrevents.repository.WaitlistRepository;
import com.example.zephyrevents.repository.RepositoryCallback;
import java.util.ArrayList;
import java.util.List;

public class MyEventsFragment extends Fragment {

    private ListView listView;
    private View myEventsEmpty;
    private ImageView myEventsEmptyIcon;
    private TextView myEventsEmptyTitle;
    private TextView myEventsEmptyHint;
    private Button tabLotteries;
    private Button tabHistory;

    private MyEventListAdapter lotteryAdapter;
    private MyEventListAdapter historyAdapter;
    private boolean showingLotteries = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_events, container, false); // Rename your layout files if you want
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.toolbar_back).setVisibility(View.INVISIBLE);

        listView = view.findViewById(R.id.my_events_list);
        myEventsEmpty = view.findViewById(R.id.my_events_empty);
        myEventsEmptyIcon = view.findViewById(R.id.my_events_empty_icon);
        myEventsEmptyTitle = view.findViewById(R.id.my_events_empty_title);
        myEventsEmptyHint = view.findViewById(R.id.my_events_empty_hint);
        tabLotteries = view.findViewById(R.id.tab_lotteries);
        tabHistory = view.findViewById(R.id.tab_history);

        tabLotteries.setOnClickListener(v -> showLotteries());
        tabHistory.setOnClickListener(v -> showHistory());

        view.findViewById(R.id.toolbar_back).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Find notification bell
        View notifBell = view.findViewById(R.id.toolbar_notifications);
        if (notifBell != null) {
            notifBell.setOnClickListener(v -> startActivity(new Intent(requireContext(), UserNotificationListView.class)));
        }

        // Make list items clickable
        listView.setOnItemClickListener((parent, v, position, id) -> {
            WaitlistEntry entry = (WaitlistEntry) parent.getItemAtPosition(position);
            if (entry != null && entry.getEventId() != null) {
                Intent intent = new Intent(requireContext(), EventDetailViewActivity.class);
                intent.putExtra(EventDetailViewActivity.EXTRA_EVENT, entry.getEventId());
                startActivity(intent);
            }
        });

        fetchUserEvents();
        showLotteries();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchUserEvents();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            onResume();
        }
    }

    private void fetchUserEvents() {
        String currentUserId = new UserController(requireContext()).getCurrentUserId();
        if (currentUserId == null) return;

        // Fetch ALL events first so we can check their dates and find ones we organize
        EventController.getInstance().getAllEvents(new RepositoryCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> allEvents) {
                new WaitlistRepository().getWaitlistsForUser(currentUserId, new RepositoryCallback<List<WaitlistEntry>>() {
                    @Override
                    public void onSuccess(List<WaitlistEntry> waitlists) {
                        List<WaitlistEntry> lotteries = new ArrayList<>();
                        List<WaitlistEntry> history = new ArrayList<>();
                        long currentTime = System.currentTimeMillis();

                        // 1. Process Waitlists (Filter by Date)
                        for (WaitlistEntry entry : waitlists) {
                            Event matchedEvent = null;
                            for (Event e : allEvents) {
                                if (e.getEventId().equals(entry.getEventId())) {
                                    matchedEvent = e;
                                    break;
                                }
                            }

                            if (matchedEvent != null) {
                                long eventTime = matchedEvent.getTime() != null ? matchedEvent.getTime().getStartTime() : 0;
                                // If event is in the future, Lottery tab. If past, History tab.
                                if (eventTime > currentTime) lotteries.add(entry);
                                else history.add(entry);
                            }
                        }

                        // 2. Process Organizer Events (Inject dummy entries so the adapter renders them)
                        for (Event event : allEvents) {
                            if (currentUserId.equals(event.getOrganizerId())) {
                                boolean alreadyInList = false;
                                for (WaitlistEntry w : waitlists) {
                                    if (w.getEventId().equals(event.getEventId())) alreadyInList = true;
                                }

                                if (!alreadyInList) {
                                    // Dummy entry. The adapter will see you are the organizer and override the status to "ORGANIZER"
                                    WaitlistEntry orgEntry = new WaitlistEntry(currentUserId, event.getEventId(), 0, 0, null);
                                    long eventTime = event.getTime() != null ? event.getTime().getStartTime() : 0;

                                    if (eventTime > currentTime) lotteries.add(orgEntry);
                                    else history.add(orgEntry);
                                }
                            }
                        }

                        // 3. Co-organizer events (same dummy pattern; adapter shows ORGANIZER badge)
                        for (Event event : allEvents) {
                            if (event.getCoOrganizerUserIds() != null
                                    && event.getCoOrganizerUserIds().contains(currentUserId)
                                    && !currentUserId.equals(event.getOrganizerId())) {
                                boolean alreadyInList = false;
                                for (WaitlistEntry w : waitlists) {
                                    if (w.getEventId().equals(event.getEventId())) alreadyInList = true;
                                }
                                for (WaitlistEntry w : lotteries) {
                                    if (w.getEventId().equals(event.getEventId())) alreadyInList = true;
                                }
                                for (WaitlistEntry w : history) {
                                    if (w.getEventId().equals(event.getEventId())) alreadyInList = true;
                                }

                                if (!alreadyInList) {
                                    WaitlistEntry coEntry = new WaitlistEntry(currentUserId, event.getEventId(), 0, 0, null);
                                    long eventTime = event.getTime() != null ? event.getTime().getStartTime() : 0;
                                    if (eventTime > currentTime) lotteries.add(coEntry);
                                    else history.add(coEntry);
                                }
                            }
                        }

                        lotteryAdapter = new MyEventListAdapter(requireContext(), lotteries);
                        historyAdapter = new MyEventListAdapter(requireContext(), history);

                        if (showingLotteries) listView.setAdapter(lotteryAdapter);
                        else listView.setAdapter(historyAdapter);
                        refreshEmptyState();
                    }
                    @Override public void onFailure(Exception e) {}
                });
            }
            @Override public void onFailure(Exception e) {}
        });
    }

    private void showLotteries() {
        showingLotteries = true;
        tabLotteries.setBackgroundResource(R.drawable.bg_tab_selected);
        tabLotteries.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        tabHistory.setBackgroundResource(R.drawable.bg_tab_unselected);
        tabHistory.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        listView.setAdapter(lotteryAdapter);
        refreshEmptyState();
    }

    private void showHistory() {
        showingLotteries = false;
        tabHistory.setBackgroundResource(R.drawable.bg_tab_selected);
        tabHistory.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        tabLotteries.setBackgroundResource(R.drawable.bg_tab_unselected);
        tabLotteries.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        listView.setAdapter(historyAdapter);
        refreshEmptyState();
    }

    private void refreshEmptyState() {
        if (myEventsEmpty == null || listView == null) return;
        MyEventListAdapter adapter = (MyEventListAdapter) listView.getAdapter();
        boolean empty = adapter == null || adapter.getCount() == 0;
        myEventsEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        listView.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (!empty || myEventsEmptyTitle == null || myEventsEmptyHint == null || myEventsEmptyIcon == null) return;
        if (showingLotteries) {
            myEventsEmptyTitle.setText(R.string.my_events_empty_lotteries);
            myEventsEmptyHint.setText(R.string.my_events_empty_lotteries_hint);
            myEventsEmptyIcon.setImageResource(R.drawable.ic_confirmation_number);
        } else {
            myEventsEmptyTitle.setText(R.string.my_events_empty_history);
            myEventsEmptyHint.setText(R.string.my_events_empty_history_hint);
            myEventsEmptyIcon.setImageResource(R.drawable.ic_calendar);
        }
    }
}