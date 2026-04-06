package com.example.zephyrevents.view.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.NotificationController;
import com.example.zephyrevents.model.NotificationType;

// Admin UI for sending or reviewing system-wide notifications; separates broadcast messaging from normal user flows.
public class AdminSystemNotificationActivity extends AppCompatActivity {

    private EditText messageInput;
    private TextView selectedTypeText;
    private LinearLayout dropdownOptions;

    private NotificationType selectedType = NotificationType.MANUAL;
    private NotificationController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_system_notification);

        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        findViewById(R.id.button_back2).setOnClickListener(v -> finish());

        messageInput = findViewById(R.id.message);
        selectedTypeText = findViewById(R.id.schedule_notification);
        dropdownOptions = findViewById(R.id.dropdown_options);

        Button sendBtn = findViewById(R.id.button_send);

        controller = new NotificationController();

        setupDropdown();
        setupSendButton(sendBtn);
    }

    private void setupDropdown() {
        selectedTypeText.setOnClickListener(v -> {
            if (dropdownOptions.getVisibility() == View.VISIBLE) {
                dropdownOptions.setVisibility(View.GONE);
            } else {
                dropdownOptions.setVisibility(View.VISIBLE);
            }
        });

        TextView option1 = findViewById(R.id.option_maintenance);
        TextView option2 = findViewById(R.id.option_policy);
        TextView option3 = findViewById(R.id.option_alert);

        option1.setOnClickListener(v -> selectType("Maintenance Notice", NotificationType.MANUAL));
        option2.setOnClickListener(v -> selectType("Policy Update", NotificationType.MANUAL));
        option3.setOnClickListener(v -> selectType("System Alert", NotificationType.MANUAL));
    }

    private void selectType(String text, NotificationType type) {
        selectedTypeText.setText(text);
        selectedType = type;
        dropdownOptions.setVisibility(View.GONE);
    }

    private void setupSendButton(Button sendBtn) {
        sendBtn.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();

            if (message.isEmpty()) {
                Toast.makeText(this, "Message is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            String fakeUserId = "ALL_USERS";
            String fakeEventId = "SYSTEM";

            controller.sendAutomaticNotification(
                    fakeUserId,
                    fakeEventId,
                    selectedType,
                    message
            );

            Toast.makeText(this, "Notification sent", Toast.LENGTH_SHORT).show();
            messageInput.setText("");
        });
    }
}