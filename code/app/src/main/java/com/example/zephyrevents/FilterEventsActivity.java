package com.example.zephyrevents;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class FilterEventsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_events);

        RecyclerView rvCategory = findViewById(R.id.rvCategory);

        rvCategory.setLayoutManager(new GridLayoutManager(this,3));
        rvCategory.setAdapter(new FilterCategoryAdapter());

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

        RadioGroup radioDate = findViewById(R.id.radioDate);

        LinearLayout customDateSelection = findViewById(R.id.customDateSelection);

        radioDate.setOnCheckedChangeListener((group, checkedId) -> {
            customDateSelection.setVisibility(checkedId == R.id.rbCustom ? View.VISIBLE : View.GONE);

        });
    }

}