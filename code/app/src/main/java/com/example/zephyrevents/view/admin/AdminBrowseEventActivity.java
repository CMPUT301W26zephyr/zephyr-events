package com.example.zephyrevents.view.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.view.event.EventDetailViewActivity;
import com.example.zephyrevents.view.adapter.EventListAdapter;

import java.util.ArrayList;
import java.util.List;


// let the admins browse events for inspection; browse pattern over events with admin-only affordances.

public class AdminBrowseEventActivity extends AppCompatActivity {
    private ListView listView;
    private EventListAdapter adapter;
    private List<Event> eventList = new ArrayList<>();
    private List<Event> displayedEvents = new ArrayList<>();
    private EditText etSearchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_browse_event);

        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        TextView title = findViewById(R.id.toolbar_title);
        if (title != null) title.setText("Browse Events");
        findViewById(R.id.btn_cancel).setVisibility(View.GONE);
        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());

        listView = findViewById(R.id.event_list);
        etSearchBar = findViewById(R.id.etSearchBar);

        adapter = new EventListAdapter(this, displayedEvents);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Event selectedEvent = displayedEvents.get(position);
            Intent intent = new Intent(this, EventDetailViewActivity.class);
            intent.putExtra(EventDetailViewActivity.EXTRA_EVENT, selectedEvent.getEventId());
            intent.putExtra("isAdminView", true);
            startActivity(intent);
        });

        if (etSearchBar != null) {
            etSearchBar.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterEvents(s.toString().trim()); }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventController.getInstance().getAllEvents(new RepositoryCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> result) {
                eventList.clear();
                if (result != null) eventList.addAll(result);
                filterEvents(etSearchBar != null ? etSearchBar.getText().toString().trim() : "");
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminBrowseEventActivity.this, "Failed to load events", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterEvents(String query) {
        displayedEvents.clear();
        if (query.isEmpty()) {
            displayedEvents.addAll(eventList);
        } else {
            String lower = query.toLowerCase();
            for (Event event : eventList) {
                if ((event.getName() != null && event.getName().toLowerCase().contains(lower)) ||
                        (event.getDescription() != null && event.getDescription().toLowerCase().contains(lower))) {
                    displayedEvents.add(event);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}