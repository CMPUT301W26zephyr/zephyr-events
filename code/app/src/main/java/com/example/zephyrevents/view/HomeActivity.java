package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.zephyrevents.R;
import com.example.zephyrevents.util.BottomNavHelper;

public class HomeActivity extends AppCompatActivity {
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
    }
}