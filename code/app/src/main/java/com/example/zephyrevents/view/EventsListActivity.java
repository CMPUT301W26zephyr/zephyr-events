package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.model.Event;

import java.util.List;

/**
 * Events List View (CRC: Events ListView).
 * Displays all events; filters by interest/availability; navigates to details; syncs updates.
 * Collaborators: EventController.
 */
public class EventsListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_events_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EventController controller = EventController.getInstance();
        List<Event> events = controller.getEventsForList();

        EventListAdapter adapter = new EventListAdapter(this, events);
        ListView listView = findViewById(R.id.event_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Event event = (Event) parent.getItemAtPosition(position);
            if (event != null && event.getEventId() != null) {
                boolean invited = controller.isInvitedEvent(event.getEventId());
                openEventDetail(event.getEventId(), invited);
            }
        });

        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());

        ImageButton searchBtn = findViewById(R.id.search_icon);
        searchBtn.setOnClickListener(v -> {
            startActivity(new Intent(EventsListActivity.this, SearchViewActivity.class));
        });

        findViewById(R.id.nav_my_events).setOnClickListener(v -> {
            startActivity(new Intent(this, MyEventsActivity.class));
        });

        ImageButton createEventBtn = findViewById(R.id.nav_create_event);
        createEventBtn.setOnClickListener(v -> {
            startActivity(new Intent(EventsListActivity.this, OrganizerEventAddEditView.class));
        });

        ImageButton profileBtn = findViewById(R.id.nav_profile);
        profileBtn.setOnClickListener(v -> {
            startActivity(new Intent(EventsListActivity.this, UserProfileViewActivity.class));
        });
    }

    /**
     * Opens the Event Detail View for the given event key.
     */
    private void openEventDetail(String eventKey, boolean invited) {
        Intent intent = new Intent(this, EventDetailViewActivity.class);
        intent.putExtra(EventDetailViewActivity.EXTRA_EVENT, eventKey);
        intent.putExtra(EventDetailViewActivity.EXTRA_INVITED, invited);
        startActivity(intent);
    }
}