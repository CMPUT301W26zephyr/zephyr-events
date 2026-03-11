package com.example.zephyrevents.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zephyrevents.R;


public class UserProfileSettingsViewActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "notification_preference";

    private static final String KEY_APP_UPDATE = "app_update";
    private static final String KEY_ORGANIZER_ANNOUNCEMENT = "organizer_announcement";
    private static final String KEY_EVENT_REMINDER = "event_remiinder";
    private static final String KEY_EVENT_CHANGES = "event_changes";
    private static final String KEY_WAITLIST_ALERTS= "waitlist_alerts";
    private static final String KEY_LOTTERY_RESULTS = "lottery_results";

    private SharedPreferences user_prefs;

    private Switch swAppUpdate;
    private Switch sworganizerannoucement;
    private Switch sweventreminders;
    private Switch sweventchanges;
    private Switch swwaitlistalert;
    private Switch swlotteryresults;








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_setting);

        user_prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        swAppUpdate = findViewById(R.id.swAppUpdate);
        sworganizerannoucement = findViewById(R.id.swOrganizerAnnouncement);
        sweventreminders = findViewById(R.id.swEventReminders);
        sweventchanges = findViewById(R.id.swEventChanges);
        swwaitlistalert = findViewById(R.id.swWaitlistAlerts);
        swlotteryresults = findViewById(R.id.swLotteryResultsNotif);


        loadSetting();
        setUpClickListener();

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
    }

    private void loadSetting(){
        swAppUpdate.setChecked(user_prefs.getBoolean(KEY_APP_UPDATE,true));
        sworganizerannoucement.setChecked(user_prefs.getBoolean(KEY_ORGANIZER_ANNOUNCEMENT,false));
        sweventreminders.setChecked(user_prefs.getBoolean(KEY_EVENT_REMINDER,true));
        sweventchanges.setChecked(user_prefs.getBoolean(KEY_EVENT_CHANGES,false));
        swwaitlistalert.setChecked(user_prefs.getBoolean(KEY_WAITLIST_ALERTS,false));
        swlotteryresults.setChecked(user_prefs.getBoolean(KEY_LOTTERY_RESULTS,true));


    }

    private void setUpClickListener(){
        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> saveSetting();
        swAppUpdate.setOnCheckedChangeListener(listener);
        sworganizerannoucement.setOnCheckedChangeListener(listener);
        sweventreminders.setOnCheckedChangeListener(listener);
        sweventchanges.setOnCheckedChangeListener(listener);
        swwaitlistalert.setOnCheckedChangeListener(listener);
        swlotteryresults.setOnCheckedChangeListener(listener);


    }

    private void saveSetting() {
        user_prefs.edit()
                .putBoolean(KEY_APP_UPDATE, swAppUpdate.isChecked())
                .putBoolean(KEY_ORGANIZER_ANNOUNCEMENT, sworganizerannoucement.isChecked())
                .putBoolean(KEY_EVENT_REMINDER, sweventreminders.isChecked())
                .putBoolean(KEY_EVENT_CHANGES, sweventchanges.isChecked())
                .putBoolean(KEY_WAITLIST_ALERTS, swwaitlistalert.isChecked())
                .putBoolean(KEY_LOTTERY_RESULTS, swlotteryresults.isChecked())
                .apply();
        }


}









