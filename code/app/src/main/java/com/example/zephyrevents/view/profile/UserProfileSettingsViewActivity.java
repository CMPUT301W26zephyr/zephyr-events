package com.example.zephyrevents.view.profile;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.example.zephyrevents.R;
import com.google.android.material.switchmaterial.SwitchMaterial;


// Screen for account/settings toggles (e.g. notifications, preferences); activity separates settings from profile viewing and editing flows.

public class UserProfileSettingsViewActivity extends AppCompatActivity {
    private SharedPreferences user_prefs;
    private SwitchMaterial swOrganizer, swLottery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_setting);

        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        TextView title = findViewById(R.id.toolbar_title);
        if (title != null) title.setText("Notification Settings");
        findViewById(R.id.btn_cancel).setVisibility(View.GONE);
        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());

        user_prefs = getSharedPreferences("notification_preference", MODE_PRIVATE);
        swOrganizer = findViewById(R.id.swOrganizerAnnouncement);
        swLottery = findViewById(R.id.swLotteryResultsNotif);

        swOrganizer.setChecked(user_prefs.getBoolean("organizer_announcement", true));
        swLottery.setChecked(user_prefs.getBoolean("lottery_results", true));

        updateToggleColor(swOrganizer);
        updateToggleColor(swLottery);

        swOrganizer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            user_prefs.edit().putBoolean("organizer_announcement", isChecked).apply();
            updateToggleColor(swOrganizer);
        });

        swLottery.setOnCheckedChangeListener((buttonView, isChecked) -> {
            user_prefs.edit().putBoolean("lottery_results", isChecked).apply();
            updateToggleColor(swLottery);
        });
    }

    private void updateToggleColor(SwitchMaterial toggle) {
        if (toggle.isChecked()) {
            toggle.setTrackTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_red)));
        } else {
            toggle.setTrackTintList(ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
        }
    }
}
