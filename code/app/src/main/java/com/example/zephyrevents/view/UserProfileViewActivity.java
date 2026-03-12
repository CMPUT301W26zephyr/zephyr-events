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
import com.example.zephyrevents.model.ContactInfo;
import com.example.zephyrevents.model.User;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.repository.UserRepository;
import com.example.zephyrevents.util.BottomNavHelper;


public class UserProfileViewActivity extends AppCompatActivity {
    private UserController userController;
    private User currentUser;

    private TextView txtName;
    private TextView txtContact;
    private ImageView avatarImg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_user);
        userController = new UserController(this);

        txtName = findViewById(R.id.txtName);
        txtContact = findViewById(R.id.txtContact);
        avatarImg = findViewById(R.id.avatar_img);

        String userId = getIntent().getStringExtra("USER");

        if (userId != null) {
            loadUser(userId);
        } else {
            txtName.setText("John Doe");
            txtContact.setText("youremail@domain.com | +01 234 567 89");
        }
        setUpClickListener();
    }

    private void loadUser(String userId){
        userController.fetchCurrentUser(new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User result){
                currentUser = result;
                updateUI(result);
            }

            @Override
            public void onFailure(Exception e){
                // If the user was out of sync and forced logged out
                if (!userController.isUserLoggedIn()) {
                    Toast.makeText(UserProfileViewActivity.this, "Session expired or account removed.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(UserProfileViewActivity.this, WelcomeActivity.class);
                    intent.putExtra("TOAST_MESSAGE", "Session expired or account removed.");  // pass to notify the user of what happened
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(UserProfileViewActivity.this, "Failed to load profile: Network error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUI(User user){
        if (user == null) return;
        txtName.setText(user.getName());

        ContactInfo contact = user.getContactInfo();

        if (contact != null){
            String email = contact.getEmail();
            String phone = contact.getPhone();
            txtContact.setText(email + "|" + phone);
        }

    }

    private void setUpClickListener(){
        findViewById(R.id.btnEditAvatar).setOnClickListener(v -> openEditProfile());
        findViewById(R.id.rowNotifications).setOnClickListener(v -> openNotifications());
        findViewById(R.id.rowEditProfile).setOnClickListener(v -> openEditProfile());
        findViewById(R.id.rowDeleteProfile).setOnClickListener(v -> showDeleteConfirmDialog());

        BottomNavHelper.setupBottomNav(this);
    }

    private void openEditProfile(){
        Intent intent = new Intent(this, UserProfileEditViewActivity.class);
        if (currentUser != null){
            intent.putExtra("USER", currentUser.getId());
        }
        startActivity(intent);
    }

    private void openNotifications(){
        Intent intent = new Intent(this, UserProfileSettingsViewActivity.class);
        startActivity(intent);
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