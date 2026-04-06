package com.example.zephyrevents.view.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import com.example.zephyrevents.view.main.MainActivity;
import com.example.zephyrevents.view.auth.WelcomeActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bumptech.glide.Glide;
import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.UserController;
import com.example.zephyrevents.util.DialogUiHelper;
import com.example.zephyrevents.repository.RepositoryCallback;

public class AdminHomeActivity extends AppCompatActivity {

    private UserController userController;
    private TextView txtName;
    private TextView txtContact;
    private ImageView avatarImg;
    private ActivityResultLauncher<PickVisualMediaRequest> pickProfileImage;
    private boolean profileHasAvatar;
    private boolean suppressRoleChanges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        userController = new UserController(this);
        txtName = findViewById(R.id.admin_txt_name);
        txtContact = findViewById(R.id.admin_txt_contact);
        avatarImg = findViewById(R.id.admin_avatar_img);

        pickProfileImage = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri == null) return;
                    userController.updateProfileImg(uri, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(AdminHomeActivity.this, "Photo updated", Toast.LENGTH_SHORT).show();
                            refreshProfile();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(AdminHomeActivity.this,
                                    "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
        );

        findViewById(R.id.admin_btn_edit_avatar).setOnClickListener(v -> {
            if (profileHasAvatar) {
                showAvatarOptionDialog();
            } else {
                launchPickProfileAvatar();
            }
        });

        RadioGroup joinAs = findViewById(R.id.admin_join_as_group);
        suppressRoleChanges = true;
        ((RadioButton) findViewById(R.id.admin_radio_admin)).setChecked(true);
        suppressRoleChanges = false;
        joinAs.setOnCheckedChangeListener((group, checkedId) -> {
            if (suppressRoleChanges) return;
            if (checkedId == R.id.admin_radio_entrant) {
                Intent i = new Intent(this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
            }
        });

        findViewById(R.id.admin_row_profiles).setOnClickListener(v ->
                startActivity(new Intent(this, AdminBrowseProfilesActivity.class)));
        findViewById(R.id.admin_row_events).setOnClickListener(v ->
                startActivity(new Intent(this, AdminBrowseEventActivity.class)));
        findViewById(R.id.admin_row_images).setOnClickListener(v ->
                startActivity(new Intent(this, AdminBrowseImageActivity.class)));
        findViewById(R.id.admin_row_logs).setOnClickListener(v ->
                startActivity(new Intent(this, AdminSystemLogActivity.class)));
        findViewById(R.id.admin_row_delete_profile).setOnClickListener(v -> showDeleteConfirmDialog());

        refreshProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshProfile();
    }

    private void refreshProfile() {
        userController.getCurrentUserProfileInfo(new RepositoryCallback<String[]>() {
            @Override
            public void onSuccess(String[] data) {
                String name = (data != null && data.length > 0 && data[0] != null) ? data[0] : "";
                String email = (data != null && data.length > 1 && data[1] != null) ? data[1] : "";
                String phone = (data != null && data.length > 2 && data[2] != null) ? data[2] : "";

                String contactinfo;
                if (!email.isEmpty() && !phone.isEmpty()) contactinfo = email + " | " + phone;
                else if (!email.isEmpty()) contactinfo = email;
                else if (!phone.isEmpty()) contactinfo = phone;
                else contactinfo = "";

                txtName.setText(name.isEmpty() ? "John Doe" : name);
                txtContact.setText(contactinfo);

                String avatarUrl = (data != null && data.length > 4 && data[4] != null) ? data[4] : "";
                boolean hasAvatar = !TextUtils.isEmpty(avatarUrl);
                profileHasAvatar = hasAvatar;

                if (hasAvatar) {
                    Glide.with(AdminHomeActivity.this)
                            .load(avatarUrl)
                            .circleCrop()
                            .into(avatarImg);
                } else {
                    Glide.with(AdminHomeActivity.this).clear(avatarImg);
                    avatarImg.setImageResource(R.drawable.ic_person_24);
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (!userController.isUserLoggedIn()) {
                    Intent intent = new Intent(AdminHomeActivity.this, WelcomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(AdminHomeActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showDeleteConfirmDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.admin_delete_yesorno, null);
        DialogUiHelper.bindAdminDeleteContent(dialogView,
                R.string.admin_delete_title_own_profile,
                R.string.admin_delete_message_own_profile);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialogView.findViewById(R.id.button_cancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.button_delete).setOnClickListener(v -> {
            dialog.dismiss();
            userController.deleteAccount(new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Intent intent = new Intent(AdminHomeActivity.this, WelcomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(AdminHomeActivity.this,
                            "Delete failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void launchPickProfileAvatar() {
        pickProfileImage.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void showConfirmRemoveAvatarDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_remove_avatar, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialogView.findViewById(R.id.btnDialogCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnDialogConfirm).setOnClickListener(v -> {
            dialog.dismiss();
            userController.clearProfileAvatar(new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Toast.makeText(AdminHomeActivity.this, "Avatar removed", Toast.LENGTH_SHORT).show();
                    refreshProfile();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(AdminHomeActivity.this,
                            "Could not remove avatar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        dialog.show();
    }

    private void showAvatarOptionDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_avatar, null);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialogView.findViewById(R.id.btnDialogCancel).setOnClickListener(v -> {
            dialog.dismiss();
            launchPickProfileAvatar();
        });

        dialogView.findViewById(R.id.btnDialogConfirm).setOnClickListener(v -> {
            dialog.dismiss();
            showConfirmRemoveAvatarDialog();
        });

        dialog.show();
    }
}
