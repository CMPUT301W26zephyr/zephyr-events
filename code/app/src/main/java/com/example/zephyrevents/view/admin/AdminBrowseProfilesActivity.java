package com.example.zephyrevents.view.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.zephyrevents.R;
import com.example.zephyrevents.model.User;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.repository.UserRepository;
import com.example.zephyrevents.view.profile.UserProfileEditViewActivity;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class AdminBrowseProfilesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminUserAdapter adapter;
    private List<User> userList = new ArrayList<>();
    private List<User> displayedList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_browse_profiles);

        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        TextView title = findViewById(R.id.toolbar_title);
        if (title != null) title.setText("Browse Profiles");
        findViewById(R.id.btn_cancel).setVisibility(View.GONE);
        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_profiles);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminUserAdapter();
        recyclerView.setAdapter(adapter);

        EditText etSearchBar = findViewById(R.id.etSearchBar);
        if (etSearchBar != null) {
            etSearchBar.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterUsers(s.toString().trim()); }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new UserRepository().getAllUsers(new RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                userList.clear();
                if (result != null) userList.addAll(result);
                EditText et = findViewById(R.id.etSearchBar);
                filterUsers(et != null ? et.getText().toString().trim() : "");
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminBrowseProfilesActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterUsers(String query) {
        displayedList.clear();
        if (query.isEmpty()) {
            displayedList.addAll(userList);
        } else {
            String lower = query.toLowerCase();
            for (User user : userList) {
                String name = user.getName() != null ? user.getName().toLowerCase() : "";
                String email = user.getContactInfo() != null && user.getContactInfo().getEmail() != null ? user.getContactInfo().getEmail().toLowerCase() : "";
                if (name.contains(lower) || email.contains(lower)) {
                    displayedList.add(user);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.VH> {
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_invite_row, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            User u = displayedList.get(position);
            String nameStr = u.getName() != null ? u.getName() : "Unknown";
            h.name.setText(nameStr);
            String detail = u.getContactInfo() != null && u.getContactInfo().getEmail() != null ? u.getContactInfo().getEmail() : "";
            h.detail.setText(detail);

            // PFP Logic
            String initial = nameStr.isEmpty() ? "?" : nameStr.substring(0, 1).toUpperCase();
            h.initial.setText(initial);

            if (u.getAvatarUrl() != null && !u.getAvatarUrl().isEmpty()) {
                com.bumptech.glide.Glide.with(h.itemView.getContext())
                        .load(u.getAvatarUrl()).circleCrop().into(h.avatar);
            } else {
                com.bumptech.glide.Glide.with(h.itemView.getContext()).clear(h.avatar);
                h.avatar.setImageDrawable(null);
            }

            h.action.setText(R.string.admin_browse_edit_profile);

            h.action.setOnClickListener(v -> {
                Intent intent = new Intent(AdminBrowseProfilesActivity.this, UserProfileEditViewActivity.class);
                intent.putExtra("userId", u.getId());
                intent.putExtra("isAdminView", true);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return displayedList.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView name, detail;
            MaterialButton action;
            TextView initial;
            ImageView avatar;

            VH(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.user_invite_name);
                detail = itemView.findViewById(R.id.user_invite_detail);
                action = itemView.findViewById(R.id.user_invite_action);
                initial = itemView.findViewById(R.id.user_invite_initial);
                avatar = itemView.findViewById(R.id.user_invite_avatar);
            }
        }
    }
}