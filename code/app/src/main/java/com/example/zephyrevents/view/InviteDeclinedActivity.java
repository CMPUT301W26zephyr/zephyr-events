package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Shown after the user declines an event invitation (lottery).
 * Adds the event to My Events → History with DECLINED. "View My Events" opens the My Events page.
 */
public class InviteDeclinedActivity extends AppCompatActivity {

    /** Intent extra key for the event name. */
    public static final String EXTRA_EVENT_NAME = "extra_event_name";
    /** Intent extra key for the event key (so we can add to History as DECLINED). */
    public static final String EXTRA_EVENT_KEY = "extra_event_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_declined);

        String eventName = getIntent().getStringExtra(EXTRA_EVENT_NAME);
        if (eventName == null) {
            eventName = "";
        }
        String eventKey = getIntent().getStringExtra(EXTRA_EVENT_KEY);
        if (eventKey != null) {
            EventController.getInstance().addDeclinedEvent(eventKey);
        }

        TextView message = findViewById(R.id.message);
        message.setText(getString(R.string.invite_declined_message, eventName));

        ImageButton back = findViewById(R.id.button_back);
        back.setOnClickListener(v -> finish());

        Button viewEvents = findViewById(R.id.button_view_events);
        viewEvents.setOnClickListener(v -> {
            startActivity(new Intent(this, MyEventsActivity.class));
            finish();
        });
    }
}
