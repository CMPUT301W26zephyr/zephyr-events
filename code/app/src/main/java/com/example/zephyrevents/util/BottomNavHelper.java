package com.example.zephyrevents.util;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.widget.ImageButton;
import androidx.core.content.ContextCompat;

import com.example.zephyrevents.R;
import com.example.zephyrevents.view.EventsListActivity;
import com.example.zephyrevents.view.HomeActivity;
import com.example.zephyrevents.view.MyEventsActivity;
import com.example.zephyrevents.view.OrganizerEventAddEditView;
import com.example.zephyrevents.view.UserProfileViewActivity;

public class BottomNavHelper {

    public static void setupBottomNav(Activity activity) {
        ImageButton navHome = activity.findViewById(R.id.nav_home);
        ImageButton navMyEvents = activity.findViewById(R.id.nav_my_events);
        ImageButton navCreateEvent = activity.findViewById(R.id.nav_create_event);
        ImageButton navProfile = activity.findViewById(R.id.nav_profile);

        int activeColor = ContextCompat.getColor(activity, R.color.primary_red);

        // 1. Home Button
        if (navHome != null) {
            if (activity instanceof HomeActivity || activity instanceof EventsListActivity) {
                navHome.setImageTintList(ColorStateList.valueOf(activeColor));
            }
            navHome.setOnClickListener(v -> {
                if (!(activity instanceof HomeActivity)) {
                    Intent intent = new Intent(activity, HomeActivity.class);
                    // FIXED: Reorder instead of clearing top so the animation override works
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(0, 0);
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
                    Intent intent = new Intent(activity, MyEventsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(0, 0);
                }
            });
        }

        // 3. Create Event Button (Still keeps default slide-up animation!)
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
                    Intent intent = new Intent(activity, UserProfileViewActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(0, 0);
                }
            });
        }
    }
}
