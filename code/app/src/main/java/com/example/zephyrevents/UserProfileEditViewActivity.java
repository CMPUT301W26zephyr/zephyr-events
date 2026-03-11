package com.example.zephyrevents;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
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
        View backBtn = findViewById(R.id.btnBack);
        backBtn.setOnClickListener(v -> finish());
        backBtn.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                finish();
            }
            return true;
        });
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
        String[] countries = {};
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
            etPhone.setText(contact.getPhone());
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
        currentUser.setLocation(country);

        userRepository.saveUser(currentUser, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(UserProfileEditViewActivity.this, "Profile saved", Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("USER", currentUser.getId());
                setResult(RESULT_OK, resultIntent);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(UserProfileEditViewActivity.this, "Failed to save profile", Toast.LENGTH_SHORT).show();



            }
        });


    }

}