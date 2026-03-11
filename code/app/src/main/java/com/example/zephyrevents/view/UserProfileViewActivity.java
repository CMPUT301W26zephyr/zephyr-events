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
import com.example.zephyrevents.model.ContactInfo;
import com.example.zephyrevents.model.User;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.repository.UserRepository;


public class UserProfileViewActivity extends AppCompatActivity {
    private UserRepository userRepository;
    private User currentUser;

    private TextView txtName;
    private TextView txtContact;
    private ImageView avatarImg;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_user);
        userRepository = new UserRepository();

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
        userRepository.getUserById(userId, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User result){
                currentUser = result;
                updateUI(result);
            }

            @Override
            public void onFailure(Exception e){
                Toast.makeText(UserProfileViewActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();


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

        View bottomNav = findViewById(R.id.bottom_nav_bar);
        if (bottomNav != null) {
            bottomNav.findViewById(R.id.bottom_nav_home).setOnClickListener(v -> {
                startActivity(new Intent(this, EventsListActivity.class));
                finish();
            });
            bottomNav.findViewById(R.id.bottom_nav_profile).setOnClickListener(v -> {
                findViewById(R.id.profile_scroll).scrollTo(0, 0);
            });
        }
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
            if (currentUser == null){
                Toast.makeText(this, "No user to delete", Toast.LENGTH_SHORT).show();
                return;

            }
            String userId = currentUser.getId();
            dialog.dismiss();
            userRepository.deleteUser(userId, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    setResult(RESULT_OK);
                    finish();


                }

                @Override
                public void onFailure(Exception e) {

                }
            });

        });
        dialog.show();


    }




}