package com.example.zephyrevents.view;



import android.content.Intent;

import android.os.Bundle;

import android.widget.Button;

import android.widget.ImageView;

import android.widget.TextView;



import androidx.activity.OnBackPressedCallback;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.content.ContextCompat;



import com.example.zephyrevents.R;

import com.example.zephyrevents.controller.EventController;

import com.example.zephyrevents.controller.UserController;



public class EventStatusActivity extends AppCompatActivity {



    public static final String EXTRA_EVENT_NAME = "extra_event_name";

    public static final String EXTRA_EVENT_KEY = "extra_event_key";

    public static final String EXTRA_STATUS_TYPE = "extra_status_type";



    /** When true with custom title, Done/back only closes this screen (event detail remains). */

    public static final String EXTRA_RETURN_TO_PREVIOUS = "extra_return_to_previous";

    public static final String EXTRA_CUSTOM_TITLE = "extra_custom_title";

    public static final String EXTRA_CUSTOM_MESSAGE = "extra_custom_message";

    /** true = green check, false = red X */

    public static final String EXTRA_STYLE_SUCCESS = "extra_style_success";



    public static final String STATUS_ACCEPTED = "ACCEPTED";

    public static final String STATUS_DECLINED = "DECLINED";



    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_event_status);



        String eventName = getIntent().getStringExtra(EXTRA_EVENT_NAME);

        if (eventName == null) eventName = "";



        String eventKey = getIntent().getStringExtra(EXTRA_EVENT_KEY);

        String statusType = getIntent().getStringExtra(EXTRA_STATUS_TYPE);



        String customTitle = getIntent().getStringExtra(EXTRA_CUSTOM_TITLE);

        String customMessage = getIntent().getStringExtra(EXTRA_CUSTOM_MESSAGE);

        boolean styleSuccess = getIntent().getBooleanExtra(EXTRA_STYLE_SUCCESS, true);



        ImageView statusIcon = findViewById(R.id.status_icon);

        TextView statusTitle = findViewById(R.id.status_title);

        TextView statusMessage = findViewById(R.id.status_message);

        Button viewEventsBtn = findViewById(R.id.button_view_events);



        String currentUserId = new UserController(this).getCurrentUserId();

        if (currentUserId == null) currentUserId = "unknown_user";



        Runnable finishDown = () -> {

            finish();

            overridePendingTransition(R.anim.activity_fade_in, R.anim.slide_out_down);

        };



        if (customTitle != null) {

            statusTitle.setText(customTitle);

            statusMessage.setText(customMessage != null ? customMessage : "");

            if (styleSuccess) {

                statusIcon.setImageResource(R.drawable.ic_check_circle);

                statusIcon.setColorFilter(ContextCompat.getColor(this, R.color.youre_in_green));

            } else {

                statusIcon.setImageResource(R.drawable.ic_cancel_circle);

                statusIcon.setColorFilter(ContextCompat.getColor(this, R.color.invite_declined_red));

            }

            viewEventsBtn.setText(R.string.status_done);

            viewEventsBtn.setOnClickListener(v -> finishDown.run());

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {

                @Override

                public void handleOnBackPressed() {

                    finishDown.run();

                }

            });

        } else if (STATUS_DECLINED.equals(statusType)) {

            statusIcon.setImageResource(R.drawable.ic_cancel_circle);

            statusIcon.setColorFilter(ContextCompat.getColor(this, R.color.invite_declined_red));

            statusTitle.setText(R.string.invite_declined_title);

            statusMessage.setText(getString(R.string.invite_declined_message, eventName));



            if (eventKey != null) {

                EventController.getInstance().addDeclinedEvent(eventKey, currentUserId);

            }

            viewEventsBtn.setOnClickListener(v -> {

                Intent intent = new Intent(this, MainActivity.class);

                intent.putExtra("TARGET_TAB", "MyEvents");

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                startActivity(intent);

                overridePendingTransition(R.anim.activity_fade_in, R.anim.slide_out_down);

                finish();

            });

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {

                @Override

                public void handleOnBackPressed() {

                    finish();

                    overridePendingTransition(R.anim.activity_fade_in, R.anim.slide_out_down);

                }

            });

        } else {

            statusIcon.setImageResource(R.drawable.ic_check_circle);

            statusIcon.setColorFilter(ContextCompat.getColor(this, R.color.youre_in_green));

            statusTitle.setText(R.string.youre_in_title);

            statusMessage.setText(getString(R.string.youre_in_message, eventName));

            viewEventsBtn.setOnClickListener(v -> {

                Intent intent = new Intent(this, MainActivity.class);

                intent.putExtra("TARGET_TAB", "MyEvents");

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                startActivity(intent);

                overridePendingTransition(R.anim.activity_fade_in, R.anim.slide_out_down);

                finish();

            });

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {

                @Override

                public void handleOnBackPressed() {

                    finish();

                    overridePendingTransition(R.anim.activity_fade_in, R.anim.slide_out_down);

                }

            });

        }



        findViewById(R.id.button_back).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

    }

}


