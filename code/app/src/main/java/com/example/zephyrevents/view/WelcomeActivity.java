package com.example.zephyrevents.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.UserController;
import com.example.zephyrevents.model.User;
import com.example.zephyrevents.view.HomeActivity;
import com.example.zephyrevents.view.SignUpActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // PLACEHOLDER (though probably usable): Check before setting the layout
//        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
//        if (prefs.getBoolean("is_signed_up", false)) {
//            // Go straight to Home and kill this activity
//            startActivity(new Intent(this, HomeActivity.class));
//            finish();
//            return; // Important: stop executing onCreate
//        }  // comment this block out for testing
        UserController userController = new UserController(this);

        if (userController.isUserLoggedIn()) {
            startActivity(new Intent(this, HomeActivity.class));
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