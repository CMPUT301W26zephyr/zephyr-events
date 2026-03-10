package com.example.zephyrevents;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        RadioGroup radioDate = findViewById(R.id.radioDate);

        LinearLayout customDateSelection = findViewById(R.id.customDateSelection);

        radioDate.setOnCheckedChangeListener((group, checkedId) -> {
            customDateSelection.setVisibility(checkedId == R.id.rbCustom ? View.VISIBLE : View.GONE);

        });
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}