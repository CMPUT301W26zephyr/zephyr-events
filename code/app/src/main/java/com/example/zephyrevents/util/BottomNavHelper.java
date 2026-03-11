package com.example.zephyrevents.view;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageButton;
import androidx.core.content.ContextCompat;

import com.example.zephyrevents.R;

public class BottomNavHelper {

    public static void setupBottomNav(Activity activity) {
        ImageButton navHome = activity.findViewById(R.id.nav_home);
        ImageButton navMyEvents = activity.findViewById(R.id.nav_my_events);
        ImageButton navCreateEvent = activity.findViewById(R.id.nav_create_event);
        ImageButton navProfile = activity.findViewById(R.id.nav_profile);

        // Get your primary red color
        int activeColor = ContextCompat.getColor(activity, R.color.primary_red);

        // 1. Home Button
        if (navHome != null) {
            if (activity instanceof HomeActivity || activity instanceof EventsListActivity) {
                navHome.setImageTintList(ColorStateList.valueOf(activeColor));
            }
            navHome.setOnClickListener(v -> {
                if (!(activity instanceof HomeActivity)) {
                    Intent intent = new Intent(activity, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                }
            });
        }

        // 2. My Events Button
        if (navMyEvents != null) {
            if (activity instanceof MyEventsActivity) {
                navMyEvents.setImageTintList(ColorStateList.valueOf(activeColor));
            }
            navMyEvents.setOnClickListener(v -> {
                if (!(activity instanceof MyEventsActivity)) {
                    activity.startActivity(new Intent(activity, MyEventsActivity.class));
                }
            });
        }

        // 3. Create Event Button
        if (navCreateEvent != null) {
            if (activity instanceof OrganizerEventAddEditView) {
                navCreateEvent.setImageTintList(ColorStateList.valueOf(activeColor));
            }
            navCreateEvent.setOnClickListener(v -> {
                if (!(activity instanceof OrganizerEventAddEditView)) {
                    activity.startActivity(new Intent(activity, OrganizerEventAddEditView.class));
                }
            });
        }

        // 4. Profile Button
        if (navProfile != null) {
            if (activity instanceof UserProfileViewActivity) {
                navProfile.setImageTintList(ColorStateList.valueOf(activeColor));
            }
            navProfile.setOnClickListener(v -> {
                if (!(activity instanceof UserProfileViewActivity)) {
                    activity.startActivity(new Intent(activity, UserProfileViewActivity.class));
                }
            });
        }
    }
}