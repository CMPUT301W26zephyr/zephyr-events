package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.UserController;

/**
 * This activity is the entry point to the application
 * Informed by userController via SharedPreferences whether tracking account locally
 * If yes, continue to HomeActivity
 * Otherwise, allow navigation to SignUpActivity
 */
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        UserController userController = new UserController(this);

        if (userController.isUserLoggedIn()) {
            Intent nextIntent;
            // Intercept FCM notification clicks to route to Event Details
            if (getIntent().getExtras() != null && getIntent().hasExtra("eventId")) {
                nextIntent = new Intent(this, EventDetailViewActivity.class);
                nextIntent.putExtra(EventDetailViewActivity.EXTRA_EVENT, getIntent().getStringExtra("eventId"));

                nextIntent.putExtra("FROM_NOTIFICATION", true);
            } else {
                nextIntent = new Intent(this, MainActivity.class);
                if (getIntent().getExtras() != null) {
                    nextIntent.putExtras(getIntent().getExtras());
                }
            }
            startActivity(nextIntent);
            finish();
            return;
        }

        // If not signed up, load the Welcome UI
        setContentView(R.layout.activity_welcome);

        // Check for any message from the previous activity
        String message = getIntent().getStringExtra("TOAST_MESSAGE");
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }

        findViewById(R.id.btn_get_started).setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, SignUpActivity.class));
        });
    }
}