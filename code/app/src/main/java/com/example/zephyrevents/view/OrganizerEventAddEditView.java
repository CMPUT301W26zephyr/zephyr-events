package com.example.zephyrevents.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.zephyrevents.R;
import com.example.zephyrevents.model.EventViewModel;

import java.util.UUID;

public class OrganizerEventAddEditView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_event);

        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

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

        EventViewModel viewModel = new ViewModelProvider(this).get(EventViewModel.class);
        String editEventId = getIntent().getStringExtra("EXTRA_EDIT_EVENT_ID");
        if (editEventId != null) {
            viewModel.isEditMode = true;
            viewModel.eventId = editEventId;
        } else if (viewModel.eventId == null) {
            viewModel.eventId = UUID.randomUUID().toString();
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