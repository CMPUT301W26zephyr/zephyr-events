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
import com.example.zephyrevents.model.WaitlistEntry;
import com.example.zephyrevents.util.BottomNavHelper;

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

        // --- NEW: Fix Toolbar Buttons ---
        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());

        // Find notification bell (Assuming standard naming, adjust ID if your XML differs)
        View notifBell = findViewById(R.id.toolbar_notifications);
        if (notifBell != null) {
            notifBell.setOnClickListener(v -> startActivity(new Intent(this, UserNotificationListView.class)));
        }

        // --- NEW: Make list items clickable ---
        listView.setOnItemClickListener((parent, view, position, id) -> {
            WaitlistEntry entry = (WaitlistEntry) parent.getItemAtPosition(position);
            if (entry != null && entry.getEventId() != null) {
                Intent intent = new Intent(this, EventDetailViewActivity.class);
                intent.putExtra(EventDetailViewActivity.EXTRA_EVENT, entry.getEventId());
                startActivity(intent);
            }
        });

        BottomNavHelper.setupBottomNav(this);

        refreshLotteryList();
        refreshHistoryList();
        showLotteries();
    }

    private void refreshLotteryList() {
        lotteryAdapter = new MyEventListAdapter(this, EventController.getInstance().getLotteryEntries());
        if (showingLotteries) listView.setAdapter(lotteryAdapter);
    }

    private void refreshHistoryList() {
        historyAdapter = new MyEventListAdapter(this, EventController.getInstance().getHistoryEntries());
        if (!showingLotteries) listView.setAdapter(historyAdapter);
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