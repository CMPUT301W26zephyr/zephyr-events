package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.model.EventStatus;
import com.example.zephyrevents.model.MyEventEntry;

import java.util.List;

/**
 * "My Events" screen opened when the user taps the ticket icon on the home screen.
 * Shows two tabs: Lotteries (events joined, not yet past registration; SELECTED/WAITING)
 * and History (placeholder cards with NOT SELECTED for now).
 * Tapping a lottery card opens that event's detail screen.
 */
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

        EventController controller = EventController.getInstance();
        List<MyEventEntry> lotteryEntries = controller.getLotteryEntries();
        List<MyEventEntry> historyEntries = controller.getHistoryEntries();
        lotteryAdapter = new MyEventListAdapter(this, lotteryEntries);
        historyAdapter = new MyEventListAdapter(this, historyEntries);

        showLotteries();

        tabLotteries.setOnClickListener(v -> showLotteries());
        tabHistory.setOnClickListener(v -> showHistory());

        listView.setOnItemClickListener((parent, view, position, id) -> {
            MyEventEntry entry = (MyEventEntry) parent.getItemAtPosition(position);
            if (entry == null || entry.isPlaceholder()) return;
            String eventKey = entry.getEventKey();
            if (eventKey != null) {
                Intent intent = new Intent(this, EventDetailViewActivity.class);
                intent.putExtra(EventDetailViewActivity.EXTRA_EVENT, eventKey);
                intent.putExtra(EventDetailViewActivity.EXTRA_INVITED, entry.getStatus() == EventStatus.SELECTED);
                startActivity(intent);
            }
        });

        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());
        findViewById(R.id.nav_home).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLotteryList();
        refreshHistoryList();
    }

    /** Refreshes the Lotteries list from the store (e.g. after leaving waitlist from event detail). */
    private void refreshLotteryList() {
        lotteryAdapter = new MyEventListAdapter(this, EventController.getInstance().getLotteryEntries());
        if (showingLotteries) {
            listView.setAdapter(lotteryAdapter);
        }
    }

    /** Refreshes the History list (e.g. after declining an invite). */
    private void refreshHistoryList() {
        historyAdapter = new MyEventListAdapter(this, EventController.getInstance().getHistoryEntries());
        if (!showingLotteries) {
            listView.setAdapter(historyAdapter);
        }
    }

    private void showLotteries() {
        showingLotteries = true;
        refreshLotteryList();
        tabLotteries.setBackgroundResource(R.drawable.bg_tab_selected);
        tabLotteries.setTextColor(ContextCompat.getColor(this, R.color.white));
        tabHistory.setBackgroundResource(R.drawable.bg_tab_unselected);
        tabHistory.setTextColor(ContextCompat.getColor(this, R.color.black));
        listView.setAdapter(lotteryAdapter);
    }

    private void showHistory() {
        showingLotteries = false;
        refreshHistoryList();
        tabHistory.setBackgroundResource(R.drawable.bg_tab_selected);
        tabHistory.setTextColor(ContextCompat.getColor(this, R.color.white));
        tabLotteries.setBackgroundResource(R.drawable.bg_tab_unselected);
        tabLotteries.setTextColor(ContextCompat.getColor(this, R.color.black));
        listView.setAdapter(historyAdapter);
    }
}
