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
import com.example.zephyrevents.model.ContactInfo;
import com.example.zephyrevents.model.User;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.repository.UserRepository;

public class UserProfileEditViewActivity extends AppCompatActivity {
    private UserRepository userRepository;
    private User currentUser;

    private EditText etName;
    private EditText etEmail;
    private Spinner spCountry;
    private EditText etPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_user);

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

        userRepository = new UserRepository();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        spCountry = findViewById(R.id.spCountry);
        etPhone = findViewById(R.id.etPhone);

        String userId = getIntent().getStringExtra("USER");

        if (userId != null){
            loadUser(userId);
        }
        setupCountrySpinner();
        setupClickListeners();
    }

    private void setupCountrySpinner() {
        String[] countries = {"None Selected", "Canada", "United States", "France"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,countries);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCountry.setAdapter(adapter);
    }

    private void loadUser(String userId) {
        userRepository.getUserById(userId, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User result) {
                currentUser = result;
                populateFields(result);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(UserProfileEditViewActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateFields(User user){
        if (user == null) return;

        etName.setText(user.getName());
        ContactInfo contact = user.getContactInfo();

        if (contact != null){
            etEmail.setText(contact.getEmail());
            if (contact.getPhone() != null) {
                etPhone.setText(contact.getPhone());
            }
        }

        if (user.getLocation() != null){
            for (int i = 0; i < spCountry.getCount(); i++){
                if (spCountry.getItemAtPosition(i).toString().equals(user.getLocation())){
                    spCountry.setSelection(i);
                    break;
                }
            }
        }
    }

    private void setupClickListeners(){
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> saveProfile());
    }

    private void saveProfile(){
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String country = spCountry.getSelectedItem().toString();

        if (currentUser == null){
            Toast.makeText(this, "Cannot save: user not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.setName(name);
        currentUser.setLocation(country.equals("None Selected") ? null : country);  //

        // Update Email and Phone number
        ContactInfo contact = currentUser.getContactInfo();
        if (contact == null) {
            contact = new ContactInfo(); // Prevent null pointer if user had no contact info initially
        }
        contact.setEmail(email);
        contact.setPhone(phone);
        currentUser.setContactInfo(contact);

        userRepository.saveUser(currentUser, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(UserProfileEditViewActivity.this, "Profile saved", Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("USER", currentUser.getId());
                setResult(RESULT_OK, resultIntent);
                finish(); // Closes activity and goes back to View Activity
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(UserProfileEditViewActivity.this, "Failed to save profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}