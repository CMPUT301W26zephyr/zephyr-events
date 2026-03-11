package com.example.zephyrevents.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zephyrevents.R;
import com.example.zephyrevents.view.HomeActivity;
import com.example.zephyrevents.view.TermsOfServiceFragment;

public class SignUpActivity extends AppCompatActivity implements TermsOfServiceFragment.OnTosAgreedListener {

    private CheckBox cbTos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        cbTos = findViewById(R.id.cb_tos);
        TextView tvTosLink = findViewById(R.id.tv_tos_link);
        Button btnContinue = findViewById(R.id.btn_continue);

        // 1. Show the Terms of Service fragment when the link is clicked
        tvTosLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TermsOfServiceFragment tosFragment = new TermsOfServiceFragment();
                tosFragment.show(getSupportFragmentManager(), "TOS_FRAGMENT");
            }
        });

        // Continue button click (Placeholder:
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cbTos.isChecked()) {
                    // Save signup state
                    SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                    prefs.edit().putBoolean("is_signed_up", true).apply();

                    // Navigate to Home Activity and clear the stack so the user can't go back
                    Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // Warn the user to accept the terms
                    Toast.makeText(SignUpActivity.this, "Please agree to the Terms and Conditions to continue.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 3. This method is called by the TermsOfServiceFragment when "I agree" is clicked
    @Override
    public void onTosAgreed() {
        cbTos.setChecked(true);
    }
}