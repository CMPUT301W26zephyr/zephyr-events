package com.example.zephyrevents.view;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zephyrevents.R;

import java.util.ArrayList;
import java.util.List;

public class AdminBrowseImageActivity extends AppCompatActivity {

    private ListView listView;
    private AdminGenericEventAdapter adapter;
    private List<Object> imageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_browse_image);

        // 🔙 Back button
        findViewById(R.id.button_back2).setOnClickListener(v -> finish());

        // 📋 ListView 연결
        listView = findViewById(R.id.event_list);

        // 📦 데이터 리스트
        imageList = new ArrayList<>();

        // 🔗 Adapter 연결
        adapter = new AdminGenericEventAdapter(
                this,
                imageList,
                R.layout.admin_event_images
        );

        listView.setAdapter(adapter);

        loadImages();
    }

    private void loadImages() {

        imageList.clear();

        // test
        for (int i = 0; i < 12; i++) {
            imageList.add("IMAGE_" + i);
        }

        adapter.notifyDataSetChanged();
    }
}