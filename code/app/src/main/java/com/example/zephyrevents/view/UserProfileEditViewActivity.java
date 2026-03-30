package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.UserController;
import com.example.zephyrevents.model.ContactInfo;
import com.example.zephyrevents.model.User;
import com.example.zephyrevents.repository.RepositoryCallback;
import android.text.TextUtils;
import com.bumptech.glide.Glide;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.widget.ImageView;

/**
 * Activity that allows the user to edit their profile.
 * Allows setting Name, Email, Country/Region, and Phone number
 * Coordinates persistence with UserController upon pressing SAVE
 */
public class UserProfileEditViewActivity extends AppCompatActivity {
    private UserController userController;

    private EditText etName;
    private EditText etEmail;
    private Spinner spCountry;
    private EditText etPhone;

    private ImageView avatarImg;
    private ActivityResultLauncher<androidx.activity.result.PickVisualMediaRequest> pickImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_user);

        avatarImg = findViewById(R.id.avatar_img);

        pickImage = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri == null) return;
                    Glide.with(this).load(uri).circleCrop().into(avatarImg);
                    userController.updateProfileImg(uri, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(UserProfileEditViewActivity.this, "Avatar Update", Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(UserProfileEditViewActivity.this,
                                    "Upload Fail" + e.getMessage(), Toast.LENGTH_LONG).show();

                        }
                    });
                }

        );

        // Customize layout_top_bar
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        if (toolbarTitle != null) {
            toolbarTitle.setText("Edit Profile");
        }
        View cancelBtnTop = findViewById(R.id.btn_cancel);
        if (cancelBtnTop != null) {
            cancelBtnTop.setVisibility(View.GONE);
        }
        View backBtn = findViewById(R.id.toolbar_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() { finish(); }
        });

        userController = new UserController(this);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        spCountry = findViewById(R.id.spCountry);
        etPhone = findViewById(R.id.etPhone);


        setupCountrySpinner();
        setupClickListeners();
        loadProfileInfo();
    }

    private void setupCountrySpinner() {
        String[] countries = {};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,countries);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCountry.setAdapter(adapter);
    }

    private void loadProfileInfo(){
        userController.getCurrentUserProfileInfo(new RepositoryCallback<String[]>() {

            @Override
            public void onSuccess(String[] data) {
                String name = data[0];
                String email = data[1];
                String phone = data[2];
                String country = data[3];

                etName.setText(name);
                etEmail.setText(email);
                etPhone.setText(phone);


                if (country != null && !country.isEmpty()) {
                    for (int i = 0; i < spCountry.getCount(); i++) {
                        if (country.equals(spCountry.getItemAtPosition(i).toString())) {
                            spCountry.setSelection(i);
                            break;
                        }
                    }
                }

                String avatarUrl = (data != null && data.length > 4 && data[4] != null) ? data[4] : "";
                if (!TextUtils.isEmpty(avatarUrl)){
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
                Toast.makeText(UserProfileEditViewActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                finish();


            }
        });
    }




    private void setupClickListeners(){
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> saveProfile());
    }

    private void saveProfile(){
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String country = spCountry.getSelectedItem() != null ? spCountry.getSelectedItem().toString() : "";

        userController.updateCurrentUserProfile(name, email, phone, country, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result){
                Toast.makeText(UserProfileEditViewActivity.this, "Profile saved", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();

            }
            @Override
            public void onFailure(Exception e){
                Toast.makeText(UserProfileEditViewActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();


            }


        });



    }
}