package com.example.zephyrevents.view;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.UserController;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.util.BottomNavHelper;

import android.text.TextUtils;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import com.bumptech.glide.Glide;



/**
 * Activity that displays the user profile.
 * Allows navigation to viewing notifications, settings, and editing profile details.
 * Allows deleting user account.
 */
public class UserProfileViewActivity extends AppCompatActivity {
    private UserController userController;

    private TextView txtName;
    private TextView txtContact;
    private ImageView avatarImg;

    private ActivityResultLauncher<PickVisualMediaRequest> pickProfileImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_user);
        userController = new UserController(this);

        txtName = findViewById(R.id.txtName);
        txtContact = findViewById(R.id.txtContact);
        avatarImg = findViewById(R.id.avatar_img);

        pickProfileImage = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri == null) return;
                    userController.updateProfileImg(uri, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(UserProfileViewActivity.this, "Photo updated", Toast.LENGTH_SHORT).show();
                            refreshProfile();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(UserProfileViewActivity.this,
                                    "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
        );



        setUpClickListener();
        refreshProfile();



    }

    @Override
    protected void onResume(){
        super.onResume();
        refreshProfile();
    }

    private void refreshProfile(){
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
                if (!TextUtils.isEmpty(avatarUrl)){
                    Glide.with(UserProfileViewActivity.this)
                            .load(avatarUrl)
                            .circleCrop()
                            .into(avatarImg);
                } else {
                    avatarImg.setImageResource(R.drawable.ic_person_24);

                }

            }

            @Override
            public void onFailure(Exception e) {
                if (!userController.isUserLoggedIn()){
                    Toast.makeText(UserProfileViewActivity.this, "Session expired or account removed.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(UserProfileViewActivity.this, WelcomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                } else{
                    Toast.makeText(UserProfileViewActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();

                }

            }
        });
    }

    private void setUpClickListener(){
        findViewById(R.id.btnEditAvatar).setOnClickListener(v ->
                pickProfileImage.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
                ));


        findViewById(R.id.rowEditProfile).setOnClickListener(v -> openEditProfile());

        // Split the Notification Actions
        findViewById(R.id.rowNotifications).setOnClickListener(v -> {
            startActivity(new Intent(this, UserNotificationListView.class));
        });
        findViewById(R.id.rowNotificationSettings).setOnClickListener(v -> {
            startActivity(new Intent(this, UserProfileSettingsViewActivity.class));
        });

        findViewById(R.id.rowTC).setOnClickListener(v -> { /* TODO: Open Terms */ });
        findViewById(R.id.rowDeleteProfile).setOnClickListener(v -> showDeleteConfirmDialog());

        BottomNavHelper.setupBottomNav(this);
    }

    private void openEditProfile(){
        startActivity(new Intent(this, UserProfileEditViewActivity.class));
    }

    private void showDeleteConfirmDialog(){
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_profile_confirm, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();
        dialogView.findViewById(R.id.btnDialogCancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnDialogConfirm).setOnClickListener(v -> {
            dialog.dismiss();

            // Use the controller to handle the logic
            userController.deleteAccount(new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
//                    Toast.makeText(UserProfileViewActivity.this, "Account Deleted", Toast.LENGTH_SHORT).show();

                    // Navigate back to WelcomeActivity and clear the stack
                    Intent intent = new Intent(UserProfileViewActivity.this, WelcomeActivity.class);
                    intent.putExtra("TOAST_MESSAGE", "Account Deleted!");  // pass to notify the user of what happened

                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(UserProfileViewActivity.this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.show();
    }
}