package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.UserController;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.util.BottomNavHelper;
import com.example.zephyrevents.model.User;

/**
 * HomeActivity is the default first screen after the authentication (log in) step
 * Shows large cards of featured events (e.g. by popularity, proximity)
 * Checks if User account still exists upon creation; returns to WelcomeActivity if not exist.
 */
public class HomeActivity extends AppCompatActivity {
    UserController userController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavHelper.setupBottomNav(this);

        TextView tvViewAll = findViewById(R.id.view_all);
        if (tvViewAll != null) {
            tvViewAll.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, EventsListActivity.class));
            });
        }

        userController = new UserController(this);

        // Fetch user data in the background to verify that account still exists
        userController.fetchCurrentUser(new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User result) {
                // User exists! Can do Personalized UI updates here (e.g. Welcome, <Name>!)
            }

            @Override
            public void onFailure(Exception e) {
                // Check if the controller wiped the session because the doc was missing
                if (!userController.isUserLoggedIn()) {
                    // Kick them back to WelcomeActivity
                    Intent intent = new Intent(HomeActivity.this, WelcomeActivity.class);
                    intent.putExtra("TOAST_MESSAGE", "Your Account Has Been Removed.");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // If it was just a normal network error (e.g., user is offline), do nothing (or something, idrk)
                }
            }
        });


    }
}