package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zephyrevents.R;

/**
 * Shown after the user accepts an event invitation (lottery).
 * Displays "You're In!" and a confirmation message; "View My Events" is implemented later.
 */
public class YoureInActivity extends AppCompatActivity {

    /** Intent extra key for the event name (e.g. "Beginner Swimming Lessons"). */
    public static final String EXTRA_EVENT_NAME = "extra_event_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youre_in);

        String eventName = getIntent().getStringExtra(EXTRA_EVENT_NAME);
        if (eventName == null) {
            eventName = "";
        }

        TextView message = findViewById(R.id.message);
        message.setText(getString(R.string.youre_in_message, eventName));

        ImageButton back = findViewById(R.id.button_back);
        back.setOnClickListener(v -> finish());

        Button viewEvents = findViewById(R.id.button_view_events);
        viewEvents.setOnClickListener(v -> {
            startActivity(new Intent(this, MyEventsActivity.class));
            finish();
        });
    }
}