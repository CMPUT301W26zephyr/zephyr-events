package com.example.zephyrevents.view;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.UserController;
import com.example.zephyrevents.model.Notification;
import com.example.zephyrevents.repository.NotificationRepository;
import com.example.zephyrevents.repository.RepositoryCallback;

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
        title.setText(R.string.notifications);

        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_cancel).setVisibility(View.GONE);

        RecyclerView recyclerView = findViewById(R.id.recycler_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String uid = new UserController(this).getCurrentUserId();
        if (uid == null) {
            Toast.makeText(this, "Sign in to see notifications.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        new NotificationRepository().getUserNotifications(uid, new RepositoryCallback<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> result) {
                List<Notification> list = result != null ? result : new ArrayList<>();
                NotificationAdapter adapter = new NotificationAdapter(list);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(UserNotificationListView.this, R.string.notifications_load_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
