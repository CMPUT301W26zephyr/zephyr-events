package com.example.zephyrevents.view;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.zephyrevents.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class OrganizerEntrantsListView extends AppCompatActivity {

    private final String[] tabTitles = new String[]{"Waitlist", "Winners", "Unregistered", "Final List"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_entrants_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.top_bar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        TextView title = findViewById(R.id.toolbar_title);
        title.setText("Event Waitlists");
        findViewById(R.id.btn_cancel).setVisibility(View.GONE);
        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager2 viewPager = findViewById(R.id.view_pager);

        viewPager.setAdapter(new EntrantsPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitles[position]);
        }).attach();

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            View tab = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(i);
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) tab.getLayoutParams();
            p.setMargins(24, 12, 24, 12);
            tab.requestLayout();
        }
    }

    private class EntrantsPagerAdapter extends FragmentStateAdapter {
        public EntrantsPagerAdapter(@NonNull AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new WaitlistFragment();
                case 1: return new WinnersFragment();
                case 2: return new UnregisteredFragment();
                case 3: return new FinalListFragment();
                default: return new WaitlistFragment();
            }
        }

        @Override
        public int getItemCount() {
            return tabTitles.length;
        }
    }
}