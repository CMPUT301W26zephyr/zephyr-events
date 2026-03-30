package com.example.zephyrevents.view;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zephyrevents.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminStatisticsActivity extends AppCompatActivity {

    private TextView newUsersCount;
    private TextView newEventsCount;
    private TextView totalRegCount;
    private TextView eventViewsCount;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_statistics);
        findViewById(R.id.button_back2).setOnClickListener(v -> finish());

        newUsersCount = findViewById(R.id.new_users_count);
        newEventsCount = findViewById(R.id.new_events_count);
        totalRegCount = findViewById(R.id.total_reg_count);
        eventViewsCount = findViewById(R.id.event_views_count);

        db = FirebaseFirestore.getInstance();

        loadStatistics();
    }

    private void loadStatistics() {

        //  1. User count
        db.collection("users")
                .get()
                .addOnSuccessListener(query -> {
                    int count = query.size();
                    newUsersCount.setText(String.valueOf(count));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show()
                );

        //  2. Event count
        db.collection("events")
                .get()
                .addOnSuccessListener(query -> {
                    int count = query.size();
                    newEventsCount.setText(String.valueOf(count));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show()
                );

        //  3. Total registrations (waitlist entries)
        db.collection("waitlist")
                .get()
                .addOnSuccessListener(query -> {
                    int count = query.size();
                    totalRegCount.setText(String.valueOf(count));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load registrations", Toast.LENGTH_SHORT).show()
                );

        //  4. Event views (placeholder for now)
        db.collection("events")
                .get()
                .addOnSuccessListener(query -> {

                    int totalViews = 0;

                    for (var doc : query) {
                        Long views = doc.getLong("views"); // field 이름 맞춰야 함
                        if (views != null) {
                            totalViews += views;
                        }
                    }

                    eventViewsCount.setText(String.valueOf(totalViews));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load views", Toast.LENGTH_SHORT).show()
                );
    }
}