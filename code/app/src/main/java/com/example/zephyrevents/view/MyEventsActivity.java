package com.example.zephyrevents.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.zephyrevents.R;
import com.example.zephyrevents.util.BottomNavHelper;

import java.util.ArrayList;

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

        BottomNavHelper.setupBottomNav(this);

        // Load temporary empty lists until Waitlist Backend is implemented
        refreshLotteryList();
        refreshHistoryList();
        showLotteries();
    }

    private void refreshLotteryList() {
        // TODO: Replace new ArrayList<>() with WaitlistRepository fetch
        lotteryAdapter = new MyEventListAdapter(this, new ArrayList<>());
        if (showingLotteries) {
            listView.setAdapter(lotteryAdapter);
        }
    }

    private void refreshHistoryList() {
        // TODO: Replace new ArrayList<>() with WaitlistRepository fetch
        historyAdapter = new MyEventListAdapter(this, new ArrayList<>());
        if (!showingLotteries) {
            listView.setAdapter(historyAdapter);
        }
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