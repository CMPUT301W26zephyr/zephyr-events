package com.example.zephyrevents.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.UserController;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.view.HomeActivity;
import com.example.zephyrevents.view.TermsOfServiceFragment;

public class SignUpActivity extends AppCompatActivity implements TermsOfServiceFragment.OnTosAgreedListener {

    private CheckBox cbTos;
    private EditText etName, etEmail, etPhone;
    private Button btnContinue;
    private UserController userController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        userController = new UserController(this);

        cbTos = findViewById(R.id.cb_tos);
        cbTos = findViewById(R.id.cb_tos);
        etName = findViewById(R.id.et_name); // Make sure IDs match your XML
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        btnContinue = findViewById(R.id.btn_continue);
        TextView tvTosLink = findViewById(R.id.tv_tos_link);

        // 1. Show the Terms of Service fragment when the link is clicked
        tvTosLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TermsOfServiceFragment tosFragment = new TermsOfServiceFragment();
                tosFragment.show(getSupportFragmentManager(), "TOS_FRAGMENT");
            }
        });

        // 2. Handle the Continue button click
        btnContinue.setOnClickListener(v -> handleSignUp());
    }

    private void handleSignUp() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Name and Email are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cbTos.isChecked()) {
            Toast.makeText(this, "Please agree to the Terms.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button while loading
        btnContinue.setEnabled(false);
        btnContinue.setText("LOADING...");

        // Pass to Controller
        userController.signUp(name, email, phone, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Navigate to Home
//                System.out.println("SHOULD BE SUCCESSFULLY CREATED BRUH");
                Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                // Re-enable button and show error
                btnContinue.setEnabled(true);
                btnContinue.setText("CONTINUE");
                Toast.makeText(SignUpActivity.this, "Sign up failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // 3. This method is called by the TermsOfServiceFragment when "I agree" is clicked
    @Override
    public void onTosAgreed() {
        cbTos.setChecked(true);
    }
}