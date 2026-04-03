package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.zephyrevents.R;
import com.example.zephyrevents.model.User;
import com.example.zephyrevents.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class AdminBrowseProfilesActivity extends AppCompatActivity {

    private ListView listView;
    private AdminGenericEventAdapter adapter;

    private List<Object> userList;
    private List<Object> displayedList;

    private EditText etSearchBar;

    private static final int REQUEST_EDIT_USER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_browse_profiles);

        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        findViewById(R.id.button_back2).setOnClickListener(v -> finish());

        listView = findViewById(R.id.event_list);

        userList = new ArrayList<>();
        displayedList = new ArrayList<>();

        adapter = new AdminGenericEventAdapter(
                this,
                displayedList,
                R.layout.admin_browse_user_item
        );

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            User selectedUser = (User) displayedList.get(position);

            Intent intent = new Intent(this, UserProfileEditViewActivity.class);
            intent.putExtra("userId", selectedUser.getId());
            intent.putExtra("isAdminView", true);

            // 🔥 changed here
            startActivityForResult(intent, REQUEST_EDIT_USER);
        });

        // Search bar setup
        etSearchBar = findViewById(R.id.etSearchBar);

        if (etSearchBar != null) {
            etSearchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterUsers(s.toString().trim());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        setupBottomNav();

        loadUsers();
    }

    // 🔥 handle result from edit screen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_EDIT_USER && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("USER_DELETED", false)) {

                // Refresh list
                loadUsers();

                // Show toast
                Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupBottomNav() {

        findViewById(R.id.nav_home).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("TARGET_TAB", "Home");
            startActivity(intent);
        });

        findViewById(R.id.nav_my_events).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("TARGET_TAB", "MyEvents");
            startActivity(intent);
        });

        findViewById(R.id.nav_create_event).setOnClickListener(v -> {
            startActivity(new Intent(this, OrganizerEventAddEditView.class));
        });

        findViewById(R.id.nav_scan_qr).setOnClickListener(v -> {
            startActivity(new Intent(this, QrScannerActivity.class));
        });

        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("TARGET_TAB", "ProfileView");
            startActivity(intent);
        });
    }

    private void loadUsers() {
        com.example.zephyrevents.repository.UserRepository repo =
                new com.example.zephyrevents.repository.UserRepository();

        repo.getAllUsers(new RepositoryCallback<List<User>>() {

            @Override
            public void onSuccess(List<User> result) {
                userList.clear();

                if (result != null) {
                    userList.addAll(result);
                }

                filterUsers("");
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(AdminBrowseProfilesActivity.this,
                                "Failed to load users",
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void filterUsers(String query) {
        displayedList.clear();

        if (query.isEmpty()) {
            displayedList.addAll(userList);
        } else {
            String lower = query.toLowerCase();

            for (Object obj : userList) {
                User user = (User) obj;

                String name = user.getName();

                String email = null;
                if (user.getContactInfo() != null) {
                    email = user.getContactInfo().getEmail();
                }

                if ((name != null && name.toLowerCase().contains(lower)) ||
                        (email != null && email.toLowerCase().contains(lower))) {
                    displayedList.add(user);
                }
            }
        }

        runOnUiThread(() -> adapter.notifyDataSetChanged());
    }
}