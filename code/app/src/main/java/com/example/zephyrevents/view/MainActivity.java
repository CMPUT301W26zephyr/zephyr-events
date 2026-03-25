package com.example.zephyrevents.view;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.zephyrevents.R;
import com.google.android.material.transition.MaterialSharedAxis;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupBottomNav();

        if (savedInstanceState == null) {
            routeIntent(getIntent(), null);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        routeIntent(intent, null);
    }

    private void routeIntent(Intent intent, Bundle savedInstanceState) {
        String targetTab = intent.getStringExtra("TARGET_TAB");

        if ("MyEvents".equals(targetTab)) {
            loadFragment(new MyEventsFragment());
        } else if ("ProfileView".equals(targetTab)) {
            loadFragment(new UserProfileViewFragment());
        } else if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void setupBottomNav() {
        findViewById(R.id.nav_home).setOnClickListener(v -> loadFragment(new HomeFragment()));
        findViewById(R.id.nav_my_events).setOnClickListener(v -> loadFragment(new MyEventsFragment()));
        findViewById(R.id.nav_profile).setOnClickListener(v -> loadFragment(new UserProfileViewFragment()));

        findViewById(R.id.nav_create_event).setOnClickListener(v -> {
            startActivity(new Intent(this, OrganizerEventAddEditView.class));
        });
    }

    private void loadFragment(Fragment fragment) {
        fragment.setEnterTransition(new com.google.android.material.transition.MaterialFadeThrough());
        getSupportFragmentManager()
                .beginTransaction()
                //.setTransition(androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.fragment_container, fragment)
                .commit();

        updateBottomNavColors(fragment);
    }

    private void updateBottomNavColors(Fragment fragment) {
        ImageButton navHome = findViewById(R.id.nav_home);
        ImageButton navMyEvents = findViewById(R.id.nav_my_events);
        ImageButton navProfile = findViewById(R.id.nav_profile);

        int activeColor = ContextCompat.getColor(this, R.color.primary_red);
        int inactiveColor = Color.parseColor("#757575");

        navHome.setImageTintList(ColorStateList.valueOf(inactiveColor));
        navMyEvents.setImageTintList(ColorStateList.valueOf(inactiveColor));
        navProfile.setImageTintList(ColorStateList.valueOf(inactiveColor));

        if (fragment instanceof HomeFragment) {
            navHome.setImageTintList(ColorStateList.valueOf(activeColor));
        } else if (fragment instanceof MyEventsFragment) {
            navMyEvents.setImageTintList(ColorStateList.valueOf(activeColor));
        } else if (fragment instanceof UserProfileViewFragment) {
            navProfile.setImageTintList(ColorStateList.valueOf(activeColor));
        }
    }
}