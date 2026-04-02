package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zephyrevents.R;
import com.example.zephyrevents.model.User;
import com.example.zephyrevents.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class AdminBrowseProfilesActivity extends AppCompatActivity {

    private ListView listView;
    private AdminGenericEventAdapter adapter;
    private List<Object> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_browse_profiles);

        findViewById(R.id.button_back2).setOnClickListener(v -> finish());

        listView = findViewById(R.id.event_list);
        userList = new ArrayList<>();

        adapter = new AdminGenericEventAdapter(
                this,
                userList,
                R.layout.admin_browse_user_item
        );

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            User selectedUser = (User) userList.get(position);

            Intent intent = new Intent(this, UserProfileEditViewActivity.class);
            intent.putExtra("userId", selectedUser.getId());
            intent.putExtra("isAdminView", true);
            startActivity(intent);
        });

        loadUsers();
    }

    private void loadUsers() {
        com.example.zephyrevents.repository.UserRepository repo =
                new com.example.zephyrevents.repository.UserRepository();

        repo.getAllUsers(new RepositoryCallback<List<User>>() {

            @Override
            public void onSuccess(List<User> result) {
                userList.clear();
                userList.addAll(result);
                runOnUiThread(() -> adapter.notifyDataSetChanged());
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
}