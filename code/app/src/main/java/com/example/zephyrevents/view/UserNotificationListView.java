package com.example.zephyrevents.view;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zephyrevents.R;
import com.example.zephyrevents.model.Notification;

import java.util.ArrayList;
import java.util.List;

public class UserNotificationListView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.top_bar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        TextView title = findViewById(R.id.toolbar_title);
        title.setText("Notifications");

        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recycler_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Notification> mockList = new ArrayList<>();

        long now = System.currentTimeMillis();

        Notification n1 = new Notification(
                "user_123",
                "event_abc",
                null,
                "You have been selected in the lottery for event_abc",
                true,
                false
        );
        n1.setTime(now - (2 * 60 * 60 * 1000));

        Notification n2 = new Notification(
                "user_123",
                "event_xyz",
                null,
                "You have been selected in the lottery for event_xyz",
                true,
                true
        );
        n2.setTime(now - (24 * 60 * 60 * 1000));

        mockList.add(n1);
        mockList.add(n2);

        NotificationAdapter adapter = new NotificationAdapter(mockList);
        recyclerView.setAdapter(adapter);
    }
}