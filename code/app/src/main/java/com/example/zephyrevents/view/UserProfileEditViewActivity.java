package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.net.Uri;

import com.bumptech.glide.Glide;
import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.SystemLogController;
import com.example.zephyrevents.controller.UserController;
import com.example.zephyrevents.model.User;
import com.example.zephyrevents.repository.RepositoryCallback;

public class UserProfileEditViewActivity extends AppCompatActivity {

    private UserController userController;

    private EditText etName;
    private EditText etEmail;
    private EditText etPhone;

    private ImageView avatarImg;
    private ActivityResultLauncher<androidx.activity.result.PickVisualMediaRequest> pickImage;

    private String adminTargetUserId = null;
    private boolean isAdminView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_user);

        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        // Get intent data
        adminTargetUserId = getIntent().getStringExtra("userId");
        isAdminView = getIntent().getBooleanExtra("isAdminView", false);

        userController = new UserController(this);

        // Set title
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        if (toolbarTitle != null) {
            toolbarTitle.setText(isAdminView ? "Profile" : "Edit Profile");
        }

        if (isAdminView) {
            View backBtn = findViewById(R.id.toolbar_back);
            View closeBtn = findViewById(R.id.btn_cancel);

            if (backBtn != null) backBtn.setVisibility(View.GONE); // No back button
            if (closeBtn != null) {
                closeBtn.setVisibility(View.VISIBLE);
                closeBtn.setOnClickListener(v -> finish());
            }
        } else {
            // Normal user → enable navigation
            View backBtn = findViewById(R.id.toolbar_back);
            if (backBtn != null) {
                backBtn.setOnClickListener(v -> finish());
            }

            View closeBtn = findViewById(R.id.btn_cancel);
            if (closeBtn != null) {
                closeBtn.setOnClickListener(v -> finish());
            }
        }

        // Handle physical back button
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        // Initialize views
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        avatarImg = findViewById(R.id.avatar_img);

        // Image picker (disabled for admin)
        pickImage = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri == null || isAdminView) return;

                    Glide.with(this).load(uri).circleCrop().into(avatarImg);

                    userController.updateProfileImg(uri, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(UserProfileEditViewActivity.this,
                                    "Avatar updated", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(UserProfileEditViewActivity.this,
                                    "Upload failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
        );

        setupClickListeners();
        loadProfileInfo();

        // Admin UI adjustments
        if (isAdminView) {

            // Disable editing
            etName.setEnabled(false);
            etEmail.setEnabled(false);
            etPhone.setEnabled(false);
            avatarImg.setEnabled(false);

            // Hide save/cancel buttons
            View cancelBtn = findViewById(R.id.btnCancel);
            View saveBtn = findViewById(R.id.btnSave);

            if (cancelBtn != null) cancelBtn.setVisibility(View.GONE);
            if (saveBtn != null) saveBtn.setVisibility(View.GONE);

            // Show delete button
            View deleteBtn = findViewById(R.id.btnDeleteUser);
            if (deleteBtn != null) deleteBtn.setVisibility(View.VISIBLE);

        }
    }

    // Setup buttons
    private void setupClickListeners() {

        View cancelBtn = findViewById(R.id.btnCancel);
        if (cancelBtn != null) {
            cancelBtn.setOnClickListener(v -> finish());
        }

        View saveBtn = findViewById(R.id.btnSave);
        if (saveBtn != null) {
            saveBtn.setOnClickListener(v -> {
                if (!isAdminView) saveProfile();
            });
        }

        View deleteBtn = findViewById(R.id.btnDeleteUser);
        if (deleteBtn != null) {
            deleteBtn.setOnClickListener(v -> {
                if (isAdminView) showDeleteDialog();
            });
        }
    }

    // Delete confirmation dialog
    private void showDeleteDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.admin_delete_yesorno, null);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Cancel → go back to list
        dialogView.findViewById(R.id.button_cancel)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                    finish(); // return to list
                });

        // Delete
        dialogView.findViewById(R.id.button_delete)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                    deleteUserAsAdmin();
                });

        dialog.show();
    }

    // Delete user
    private void deleteUserAsAdmin() {
        if (adminTargetUserId == null) return;

        new com.example.zephyrevents.repository.UserRepository()
                .deleteUser(adminTargetUserId, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        runOnUiThread(() -> {

                            String deletedName = etName.getText() != null ? etName.getText().toString() : "Unknown User";
                            SystemLogController.getInstance()
                                    .logAction("ADMIN_DELETED_PROFILE", "Admin deleted user profile: " + deletedName, "Admin");

                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("USER_DELETED", true);
                            setResult(RESULT_OK, resultIntent);

                            Toast.makeText(UserProfileEditViewActivity.this,
                                    "User deleted", Toast.LENGTH_SHORT).show();

                            finish();
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(UserProfileEditViewActivity.this,
                                        "Delete failed", Toast.LENGTH_SHORT).show()
                        );
                    }
                });
    }

    // Load profile
    private void loadProfileInfo() {

        if (isAdminView && adminTargetUserId != null) {

            new com.example.zephyrevents.repository.UserRepository()
                    .getUserById(adminTargetUserId, new RepositoryCallback<User>() {

                        @Override
                        public void onSuccess(User user) {
                            runOnUiThread(() -> {

                                if (user == null) return;

                                etName.setText(user.getName());

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

                                etEmail.setText(email);
                                etPhone.setText(phone);

                                if (!TextUtils.isEmpty(user.getAvatarUrl())) {
                                    Glide.with(UserProfileEditViewActivity.this)
                                            .load(user.getAvatarUrl())
                                            .circleCrop()
                                            .into(avatarImg);
                                }
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(UserProfileEditViewActivity.this,
                                    "Failed to load profile", Toast.LENGTH_SHORT).show();
                        }
                    });

            return;
        }

        userController.getCurrentUserProfileInfo(new RepositoryCallback<String[]>() {
            @Override
            public void onSuccess(String[] data) {
                etName.setText(data[0]);
                etEmail.setText(data[1]);
                etPhone.setText(data[2]);

                String avatarUrl = (data.length > 4) ? data[4] : "";

                if (!TextUtils.isEmpty(avatarUrl)) {
                    Glide.with(UserProfileEditViewActivity.this)
                            .load(avatarUrl)
                            .circleCrop()
                            .into(avatarImg);
                } else {
                    avatarImg.setImageResource(R.drawable.ic_person_24);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(UserProfileEditViewActivity.this,
                        "Failed to load profile",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        userController.updateCurrentUserProfile(name, email, phone, "", new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(UserProfileEditViewActivity.this, "Profile saved", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(UserProfileEditViewActivity.this, "Save failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}