package com.example.zephyrevents.view.notif;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.UserController;
import com.example.zephyrevents.model.Notification;
import com.example.zephyrevents.repository.NotificationRepository;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.view.adapter.NotificationAdapter;

import java.util.ArrayList;
import java.util.List;

public class UserNotificationListView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        TextView title = findViewById(R.id.toolbar_title);
        title.setText(R.string.notifications);

        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recycler_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String uid = new UserController(this).getCurrentUserId();

        android.widget.ImageButton btnClear = findViewById(R.id.btn_cancel);
        btnClear.setImageResource(R.drawable.ic_delete_24);
        btnClear.setVisibility(View.VISIBLE);
        btnClear.setOnClickListener(v -> {
            if (uid != null) {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                        .setTitle("Clear Notifications")
                        .setMessage("Are you sure you want to delete all notifications?")
                        .setPositiveButton("Clear All", (dialog, which) -> {
                            new NotificationRepository().deleteAllUserNotifications(uid, new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    Toast.makeText(UserNotificationListView.this, "All notifications cleared", Toast.LENGTH_SHORT).show();
                                    recyclerView.setAdapter(new NotificationAdapter(new ArrayList<>()));
                                }
                                @Override
                                public void onFailure(Exception e) {}
                            });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        if (uid == null) {
            Toast.makeText(this, "Sign in to see notifications.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        new NotificationRepository().markAllAsRead(uid);

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
