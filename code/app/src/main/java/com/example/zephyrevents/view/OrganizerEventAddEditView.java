package com.example.zephyrevents.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.zephyrevents.R;
import com.example.zephyrevents.model.EventViewModel;

public class OrganizerEventAddEditView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);  // Makes it do weird stuff with hole punch camera
        setContentView(R.layout.activity_organizer_event);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            return insets;
        });

        findViewById(R.id.toolbar_back).setOnClickListener(v -> {
            FragmentManager fm = getSupportFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
            } else {
                // Fallback if no fragment to pop, perhaps finish activity
                finish();
            }
        });
        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());

        //  Check if we are editing an existing event
        String editEventId = getIntent().getStringExtra("EXTRA_EDIT_EVENT_ID");
        if (editEventId != null) {
            EventViewModel viewModel = new androidx.lifecycle.ViewModelProvider(this).get(EventViewModel.class);
            viewModel.isEditMode = true;
            viewModel.eventId = editEventId;
        }

        if (savedInstanceState == null) {
            navigateToFragment(new EventCreateFragment(), false);
        }
    }

    public void navigateToFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_MATCH_ACTIVITY_OPEN);
        transaction.replace(R.id.fragment_container, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    public void setupTopAndBottomUI(String titleText, String buttonText, View.OnClickListener onNextClick) {
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(titleText);

        Button nextBtn = findViewById(R.id.next_button);
        nextBtn.setText(buttonText);
        nextBtn.setOnClickListener(onNextClick);
    }
}