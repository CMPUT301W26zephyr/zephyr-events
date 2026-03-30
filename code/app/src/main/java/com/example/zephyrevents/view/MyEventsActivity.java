package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.model.WaitlistEntry;
import com.example.zephyrevents.util.BottomNavHelper;

import com.example.zephyrevents.controller.UserController;
import com.example.zephyrevents.repository.WaitlistRepository;
import com.example.zephyrevents.repository.RepositoryCallback;
import java.util.ArrayList;
import java.util.List;

public class MyEventsActivity extends AppCompatActivity {

    private ListView listView;
    private Button tabLotteries;
    private Button tabHistory;

    private MyEventListAdapter lotteryAdapter;
    private MyEventListAdapter historyAdapter;
    private boolean showingLotteries = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        listView = findViewById(R.id.my_events_list);
        tabLotteries = findViewById(R.id.tab_lotteries);
        tabHistory = findViewById(R.id.tab_history);

        tabLotteries.setOnClickListener(v -> showLotteries());
        tabHistory.setOnClickListener(v -> showHistory());

        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());

        // Find notification bell
        View notifBell = findViewById(R.id.toolbar_notifications);
        if (notifBell != null) {
            notifBell.setOnClickListener(v -> startActivity(new Intent(this, UserNotificationListView.class)));
        }

        // Make list items clickable
        listView.setOnItemClickListener((parent, view, position, id) -> {
            WaitlistEntry entry = (WaitlistEntry) parent.getItemAtPosition(position);
            if (entry != null && entry.getEventId() != null) {
                Intent intent = new Intent(this, EventDetailViewActivity.class);
                intent.putExtra(EventDetailViewActivity.EXTRA_EVENT, entry.getEventId());
                startActivity(intent);
            }
        });

        BottomNavHelper.setupBottomNav(this);

        fetchUserEvents();
        showLotteries();
    }

    @Override
    protected void onResume(){
        super.onResume();
        fetchUserEvents();
    }

    private void fetchUserEvents() {
        String currentUserId = new UserController(this).getCurrentUserId();
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

                        lotteryAdapter = new MyEventListAdapter(MyEventsActivity.this, lotteries);
                        historyAdapter = new MyEventListAdapter(MyEventsActivity.this, history);

                        if (showingLotteries) listView.setAdapter(lotteryAdapter);
                        else listView.setAdapter(historyAdapter);
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
        tabLotteries.setTextColor(ContextCompat.getColor(this, R.color.white));
        tabHistory.setBackgroundResource(R.drawable.bg_tab_unselected);
        tabHistory.setTextColor(ContextCompat.getColor(this, R.color.black));
        listView.setAdapter(lotteryAdapter);
    }

    private void showHistory() {
        showingLotteries = false;
        tabHistory.setBackgroundResource(R.drawable.bg_tab_selected);
        tabHistory.setTextColor(ContextCompat.getColor(this, R.color.white));
        tabLotteries.setBackgroundResource(R.drawable.bg_tab_unselected);
        tabLotteries.setTextColor(ContextCompat.getColor(this, R.color.black));
        listView.setAdapter(historyAdapter);
    }
}