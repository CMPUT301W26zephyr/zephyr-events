package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

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

        UserController userController = new UserController(this);

        if (userController.isUserLoggedIn()) {
            Intent mainIntent = new Intent(this, MainActivity.class);
            // Forward the notification click data to MainActivity
            if (getIntent().getExtras() != null) {
                mainIntent.putExtras(getIntent().getExtras());
            }
            startActivity(mainIntent);
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