package com.example.zephyrevents.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.zephyrevents.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminTermsConditionsActivity extends AppCompatActivity {

    private EditText termsContent;
    private Button editButton;

    private boolean isEditing = false;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_termsandconditions);

        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        // Back button
        findViewById(R.id.button_back2).setOnClickListener(v -> finish());

        termsContent = findViewById(R.id.terms_content);
        editButton = findViewById(R.id.button_edit);

        db = FirebaseFirestore.getInstance();

        // Disable editing initially
        termsContent.setEnabled(false);

        loadTerms();
        setupEditButton();
    }

    private void loadTerms() {

        db.collection("settings")
                .document("terms")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String text = doc.getString("content");
                        if (text != null) {
                            termsContent.setText(text);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load terms", Toast.LENGTH_SHORT).show()
                );
    }

    private void setupEditButton() {

        editButton.setOnClickListener(v -> {

            if (!isEditing) {
                // Switch to edit mode
                isEditing = true;
                termsContent.setEnabled(true);
                editButton.setText("Save");
            } else {
                // Save changes
                isEditing = false;
                termsContent.setEnabled(false);
                editButton.setText("Edit");

                saveTerms();
            }
        });
    }

    private void saveTerms() {

        String updatedText = termsContent.getText().toString().trim();

        db.collection("settings")
                .document("terms")
                .update("content", updatedText)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                );
    }
}