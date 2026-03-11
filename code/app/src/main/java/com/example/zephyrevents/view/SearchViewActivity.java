package com.example.zephyrevents.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zephyrevents.R;
import com.example.zephyrevents.model.Event;

import java.util.ArrayList;
import java.util.List;


public class SearchViewActivity extends AppCompatActivity {
    private static final String SEARCH_PREFS = "search_history";
    private static final String KEY_HISTORY = "history";

    private EditText etSearchBar;
    private RecyclerView rvContent;
    private SearchEventListAdapter eventAdapter;
    private SharedPreferences prefs;
    private List<Event> allEvents = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_events);

        prefs = getSharedPreferences(SEARCH_PREFS, MODE_PRIVATE);

        etSearchBar = findViewById(R.id.etSearchBar);
        rvContent = findViewById(R.id.rvContent);

        rvContent.setLayoutManager(new LinearLayoutManager(this));

        eventAdapter = new SearchEventListAdapter(new ArrayList<>());

        rvContent.setAdapter(eventAdapter);

        loadSampleEvents();

        View backBtn = findViewById(R.id.btnBack);
        backBtn.setOnClickListener(v -> finish());
        backBtn.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                finish();
            }
            return true;
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() { finish(); }
        });

        findViewById(R.id.btnSearchFilter).setOnClickListener(v -> {
            startActivity(new Intent(this, FilterEventsActivity.class));
        });


        etSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable S) {
            }

        });
    }

    private void loadSampleEvents() {
        long now = System.currentTimeMillis();
        long day = 24 * 60 * 60 * 1000L;

        allEvents.add(new Event("1", "Category", "Category", now + day, now + day + 3600000));
        allEvents.add(new Event("1", "Swimming Lessons", "Learn to swim", now + day, now + day + 3600000));
        allEvents.add(new Event("1", "Swimming Lessons", "Learn to swim", now + day, now + day + 3600000));
        allEvents.add(new Event("1", "Swimming Lessons", "Learn to swim", now + day, now + day + 3600000));


        filterEvents("");
    }

    private void filterEvents(String query) {
        List<Event> filtered = new ArrayList<>();
        String lower = query.toLowerCase();
        for (Event e : allEvents) {
            if (query.isEmpty() ||
                    (e.getName() != null && e.getName().toLowerCase().contains(lower)) ||
                    (e.getDescription() != null && e.getDescription().toLowerCase().contains(lower))) {
                filtered.add(e);
            }
        }
        eventAdapter.updateEvents(filtered);
    }




}