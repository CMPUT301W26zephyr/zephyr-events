package com.example.zephyrevents.view;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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
        setContentView(R.layout.activity_notifications);

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
                new NotificationRepository().deleteAllUserNotifications(uid, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(UserNotificationListView.this, "All notifications cleared", Toast.LENGTH_SHORT).show();
                        recyclerView.setAdapter(new NotificationAdapter(new ArrayList<>()));
                    }
                    @Override
                    public void onFailure(Exception e) {}
                });
            }
        });

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
