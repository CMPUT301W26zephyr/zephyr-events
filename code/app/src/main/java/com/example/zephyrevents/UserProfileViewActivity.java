package com.example.zephyrevents;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
        findViewById(R.id.rowTC).setOnClickListener(v -> openTermsAndConditions());
        findViewById(R.id.rowDeleteProfile).setOnClickListener(v -> showDeleteConfirmDialog());



    }

    private void openEditProfile(){
        Intent intent = new Intent(this, UserProfileEditViewActivity.class);
        if (currentUser != null){
            intent.putExtra("USER", currentUser.getId());
        }
        startActivity(intent);
    }

    private void openNotifications(){
        Intent intent = new Intent(this, NotifcationsSettingActivity.class);
        startActivity(intent);
    }

    private void openTermsAndConditions() {
        Toast.makeText(this, "Terms and Conditions", Toast.LENGTH_SHORT).show();
        // TODO: Open Terms screen or WebView
    }




}