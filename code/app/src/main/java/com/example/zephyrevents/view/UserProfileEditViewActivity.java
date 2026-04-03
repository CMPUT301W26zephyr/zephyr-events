package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.UserController;
import com.example.zephyrevents.repository.RepositoryCallback;

import android.text.TextUtils;
import com.bumptech.glide.Glide;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.widget.ImageView;

public class UserProfileEditViewActivity extends AppCompatActivity {

    private UserController userController;

    private EditText etName;
    private EditText etEmail;
    private Spinner spCountry;
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

        adminTargetUserId = getIntent().getStringExtra("userId");
        isAdminView = getIntent().getBooleanExtra("isAdminView", false);

        userController = new UserController(this);

        // 🔥 title 변경
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        if (toolbarTitle != null) {
            toolbarTitle.setText(isAdminView ? "Profile" : "Edit Profile");
        }

        // 🔥 버튼 숨김
        View cancelBtn = findViewById(R.id.btnCancel);
        View saveBtn = findViewById(R.id.btnSave);

        if (isAdminView) {
            if (cancelBtn != null) cancelBtn.setVisibility(View.GONE);
            if (saveBtn != null) saveBtn.setVisibility(View.GONE);
        }

        // 🔥 delete 버튼 표시
        View deleteBtn = findViewById(R.id.btnDeleteUser);
        deleteBtn.setVisibility(isAdminView ? View.VISIBLE : View.GONE);

        avatarImg = findViewById(R.id.avatar_img);

        pickImage = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri == null || isAdminView) return;
                    Glide.with(this).load(uri).circleCrop().into(avatarImg);
                    userController.updateProfileImg(uri, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(UserProfileEditViewActivity.this, "Avatar Update", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(UserProfileEditViewActivity.this,
                                    "Upload Fail: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
        );

        View backBtn = findViewById(R.id.toolbar_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> goBack());
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goBack();
            }
        });

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        spCountry = findViewById(R.id.spCountry);
        etPhone = findViewById(R.id.etPhone);

        setupCountrySpinner();
        setupClickListeners();
        loadProfileInfo();

        // 🔥 admin이면 완전 비활성화
        if (isAdminView) {
            etName.setEnabled(false);
            etEmail.setEnabled(false);
            etPhone.setEnabled(false);
            spCountry.setEnabled(false);
            avatarImg.setEnabled(false);
        }
    }

    private void goBack() {
        if (isAdminView) {
            startActivity(new Intent(this, AdminBrowseProfilesActivity.class));
            finish();
        } else {
            finish();
        }
    }

    private void setupCountrySpinner() {
        String[] countries = {};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countries);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCountry.setAdapter(adapter);
    }

    private void setupClickListeners() {
        findViewById(R.id.btnCancel).setOnClickListener(v -> goBack());

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            if (!isAdminView) {
                saveProfile();
            }
        });

        findViewById(R.id.btnDeleteUser).setOnClickListener(v -> {
            if (isAdminView) {
                showAdminDeleteDialog();
            }
        });
    }

    private void showAdminDeleteDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.admin_delete_yesorno, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialogView.findViewById(R.id.button_cancel)
                .setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.button_delete)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                    deleteUserAsAdmin();
                });

        dialog.show();
    }

    private void deleteUserAsAdmin() {
        if (adminTargetUserId == null) return;

        new com.example.zephyrevents.repository.UserRepository()
                .deleteUser(adminTargetUserId, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        runOnUiThread(() -> {
                            Toast.makeText(UserProfileEditViewActivity.this,
                                    "User deleted",
                                    Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(UserProfileEditViewActivity.this,
                                    AdminBrowseProfilesActivity.class));
                            finish();
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(UserProfileEditViewActivity.this,
                                        "Delete failed",
                                        Toast.LENGTH_SHORT).show()
                        );
                    }
                });
    }

    private void loadProfileInfo() {
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
        String country = spCountry.getSelectedItem() != null ? spCountry.getSelectedItem().toString() : "";

        userController.updateCurrentUserProfile(name, email, phone, country, new RepositoryCallback<Void>() {
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