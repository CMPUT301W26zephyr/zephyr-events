package com.example.zephyrevents;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.repository.EventRepository;
import com.example.zephyrevents.repository.RepositoryCallback;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SearchViewActivity extends AppCompatActivity {
    private static final String SEARCH_PREFS = "search_history";
    private static final String KEY_HISTORY = "history";

    private EditText etSearchBar;
    private LinearLayout historyTagsContainer;
    private RecyclerView rvContent;
    private EventListAdapter eventAdapter;
    private SharedPreferences prefs;
    private List<Event> allEvents = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_events);

        prefs = getSharedPreferences(SEARCH_PREFS, MODE_PRIVATE);

        etSearchBar = findViewById(R.id.etSearchBar);
        historyTagsContainer = findViewById(R.id.historyTagsContainer);
        rvContent = findViewById(R.id.rvContent);

        rvContent.setLayoutManager(new LinearLayoutManager(this));

        eventAdapter = new EventListAdapter(new ArrayList<>());

        rvContent.setAdapter(eventAdapter);

        updateHistoryTags();
        loadSampleEvents();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        findViewById(R.id.btnSearchFilter).setOnClickListener(v -> {
            startActivity(new Intent(this, FilterEventsActivity.class));
        });

        findViewById(R.id.btnClearSearch).setOnClickListener(v -> {
            prefs.edit().remove(KEY_HISTORY).apply();
            updateHistoryTags();
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

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void addToHistory(String history_term) {
        if (history_term.isEmpty()) return;

        Set<String> history = prefs.getStringSet(KEY_HISTORY, new HashSet<>());
        Set<String> updated = new HashSet<>(history);
        updated.remove(history_term);
        updated.add(history_term);

        if (updated.size() > 9) {
            List<String> list = new ArrayList<>(updated);
            updated = new HashSet<>(list.subList(list.size() - 9, list.size()));
        }
        prefs.edit().putStringSet(KEY_HISTORY, updated).apply();
        updateHistoryTags();


    }

    private void updateHistoryTags() {
        historyTagsContainer.removeAllViews();
        Set<String> history = prefs.getStringSet(KEY_HISTORY, new HashSet<>());
        for (String tag : history) {
            View chip = getLayoutInflater().inflate(R.layout.item_history_tag, historyTagsContainer, false);
            android.widget.TextView txt = chip.findViewById(R.id.txtHistoryTag);
            txt.setText(tag);
            chip.setOnClickListener(v -> {
                etSearchBar.setText(tag);
                addToHistory(tag);
                filterEvents(tag);
            });
            historyTagsContainer.addView(chip);


        }
    }
}