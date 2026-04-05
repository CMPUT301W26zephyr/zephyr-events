package com.example.zephyrevents.view;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.zephyrevents.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class OrganizerEntrantsListView extends AppCompatActivity {

    private final String[] tabTitles = new String[]{"Waitlist", "Winners", "Unregistered", "Final List"};
    private String eventId;
    private TabLayout tabLayout;

    /**
     * Called by {@link EntrantsListFragment} when waitlist data loads so tab labels show counts, e.g. Waitlist (3).
     */
    public void updateTabCount(int tabIndex, int count) {
        if (tabLayout == null || tabIndex < 0 || tabIndex >= tabTitles.length) return;
        TabLayout.Tab tab = tabLayout.getTabAt(tabIndex);
        if (tab != null) {
            tab.setText(tabTitles[tabIndex] + " (" + count + ")");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_entrants_list);

        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        // Retrieve the Event ID passed from EventDetailViewActivity
        eventId = getIntent().getStringExtra(EventDetailViewActivity.EXTRA_EVENT);

        TextView title = findViewById(R.id.toolbar_title);
        title.setText("Entrants List");

        findViewById(R.id.btn_cancel).setVisibility(View.GONE);
        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());

        tabLayout = findViewById(R.id.tab_layout);
        ViewPager2 viewPager = findViewById(R.id.view_pager);

        viewPager.setAdapter(new EntrantsPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitles[position] + " (0)");
        }).attach();
    }

    private class EntrantsPagerAdapter extends FragmentStateAdapter {
        public EntrantsPagerAdapter(@NonNull AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // Uses the dynamic fragment we just created
            return EntrantsListFragment.newInstance(position, eventId);
        }

        @Override
        public int getItemCount() {
            return tabTitles.length;
        }
    }
}