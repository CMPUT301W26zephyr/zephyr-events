package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.zephyrevents.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        setupBottomNav();
    }

    private void setupBottomNav() {
        findViewById(R.id.nav_home).setOnClickListener(v -> loadFragment(new HomeFragment()));
        //findViewById(R.id.nav_my_events).setOnClickListener(v -> loadFragment(new MyEventsFragment()));
        //findViewById(R.id.nav_profile).setOnClickListener(v -> loadFragment(new UserProfileFragment()));

        // Keep OrganizerEventAddEditView as an Activity if it's a full-screen form!
        findViewById(R.id.nav_create_event).setOnClickListener(v -> {
            startActivity(new Intent(this, OrganizerEventAddEditView.class));
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}