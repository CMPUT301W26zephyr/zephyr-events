package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.example.zephyrevents.R;

public class AdminHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        findViewById(R.id.btn_profiles).setOnClickListener(v -> startActivity(new Intent(this, AdminBrowseProfilesActivity.class)));
        findViewById(R.id.btn_events).setOnClickListener(v -> startActivity(new Intent(this, AdminBrowseEventActivity.class)));
        findViewById(R.id.btn_images).setOnClickListener(v -> startActivity(new Intent(this, AdminBrowseImageActivity.class)));
        findViewById(R.id.btn_logs).setOnClickListener(v -> startActivity(new Intent(this, AdminSystemLogActivity.class)));

        // Return to User View
        findViewById(R.id.btn_return_user).setOnClickListener(v -> finish());
    }
}