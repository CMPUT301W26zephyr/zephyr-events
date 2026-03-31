package com.example.zephyrevents.view;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zephyrevents.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AdminSystemLogActivity extends AppCompatActivity {

    private LinearLayout logContainer;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_systemlog);

        // Back button
        findViewById(R.id.button_back2).setOnClickListener(v -> finish());

        logContainer = findViewById(R.id.log_container);
        db = FirebaseFirestore.getInstance();

        loadLogs();
    }

    private void loadLogs() {

        db.collection("logs")
                .get()
                .addOnSuccessListener(query -> {

                    // Keep existing UI logs if you want
                    // logContainer.removeAllViews(); ← DO NOT use if UI must stay

                    for (QueryDocumentSnapshot doc : query) {

                        String message = doc.getString("message");
                        String type = doc.getString("type");
                        String timestamp = doc.getString("timestamp");

                        addLogItem(message, type, timestamp);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load logs", Toast.LENGTH_SHORT).show()
                );
    }

    private void addLogItem(String message, String type, String timestamp) {

        // Create layout programmatically (NO extra XML needed)
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 0, 0, 16);

        View dot = new View(this);
        LinearLayout.LayoutParams dotParams =
                new LinearLayout.LayoutParams(10, 10);
        dotParams.setMargins(0, 8, 8, 0);
        dot.setLayoutParams(dotParams);
        dot.setBackgroundColor(getColorByType(type));

        TextView text = new TextView(this);
        text.setText(timestamp + "\n" + message);
        text.setTextSize(12);

        LinearLayout.LayoutParams textParams =
                new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        text.setLayoutParams(textParams);

        row.addView(dot);
        row.addView(text);

        logContainer.addView(row);
    }

    private int getColorByType(String type) {

        if (type == null) return Color.GRAY;

        switch (type) {
            case "created":
                return Color.RED;
            case "updated":
                return Color.parseColor("#FF9800");
            case "cancelled":
                return Color.YELLOW;
            case "selected":
                return Color.GREEN;
            case "admin":
                return Color.BLUE;
            default:
                return Color.GRAY;
        }
    }
}