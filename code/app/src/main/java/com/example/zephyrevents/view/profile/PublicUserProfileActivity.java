package com.example.zephyrevents.view.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.zephyrevents.R;
import com.example.zephyrevents.model.User;
import com.example.zephyrevents.repository.EventRepository;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.repository.UserRepository;

// Read-only profile when viewing another user (e.g. from an event or list); activity pattern carries user id via intent and avoids mixing with self-profile UI.

/**
 * Read-only profile for an event organizer (or any user id), shown from the event detail card.
 */
public class PublicUserProfileActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "extra_user_id";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_profile);

        String userId = getIntent().getStringExtra(EXTRA_USER_ID);
        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(this, R.string.public_profile_unavailable, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView title = findViewById(R.id.toolbar_title);
        if (title != null) {
            title.setText(R.string.public_profile_title);
        }
        View back = findViewById(R.id.toolbar_back);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }
        View cancel = findViewById(R.id.btn_cancel);
        if (cancel != null) {
            cancel.setVisibility(View.GONE);
        }

        ImageView avatar = findViewById(R.id.public_profile_avatar);
        TextView nameView = findViewById(R.id.public_profile_name);
        TextView countView = findViewById(R.id.public_profile_events_count);
        TextView emailView = findViewById(R.id.public_profile_email);
        TextView phoneView = findViewById(R.id.public_profile_phone);
        phoneView.setVisibility(View.GONE);

        UserRepository userRepository = new UserRepository();
        EventRepository eventRepository = new EventRepository();

        userRepository.getUserById(userId, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    Toast.makeText(PublicUserProfileActivity.this, R.string.public_profile_unavailable, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                String name = user.getName() != null ? user.getName() : getString(R.string.placeholder);
                nameView.setText(name);

                String email = "";
                String phone = "";
                if (user.getContactInfo() != null) {
                    if (user.getContactInfo().getEmail() != null) {
                        email = user.getContactInfo().getEmail();
                    }
                    if (user.getContactInfo().getPhone() != null) {
                        phone = user.getContactInfo().getPhone();
                    }
                }
                emailView.setText(email);
                if (phone.isEmpty()) {
                    phoneView.setText("");
                    phoneView.setVisibility(View.GONE);
                } else {
                    phoneView.setVisibility(View.VISIBLE);
                    phoneView.setText(phone);
                }

                String url = user.getAvatarUrl();
                if (url != null && !url.trim().isEmpty()) {
                    Glide.with(PublicUserProfileActivity.this)
                            .load(url.trim())
                            .circleCrop()
                            .placeholder(R.drawable.ic_person_24)
                            .error(R.drawable.ic_person_24)
                            .into(avatar);
                } else {
                    avatar.setImageResource(R.drawable.ic_person_24);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(PublicUserProfileActivity.this, R.string.public_profile_unavailable, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        eventRepository.countEventsOrganizedBy(userId, new RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                int n = count != null ? count : 0;
                countView.setText(getResources().getQuantityString(R.plurals.events_organized_count, n, n));
            }

            @Override
            public void onFailure(Exception e) {
                countView.setText(getResources().getQuantityString(R.plurals.events_organized_count, 0, 0));
            }
        });
    }
}
