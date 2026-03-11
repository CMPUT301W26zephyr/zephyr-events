package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zephyrevents.R;
import com.example.zephyrevents.view.EventsListActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView tvViewAll = findViewById(R.id.view_all);

        if (tvViewAll != null) {
            tvViewAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to the Events List Activity
                    Intent intent = new Intent(HomeActivity.this, EventsListActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Initialize other Home components here...
    }
}