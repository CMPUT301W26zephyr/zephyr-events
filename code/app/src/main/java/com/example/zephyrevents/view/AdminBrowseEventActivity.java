package com.example.zephyrevents.view;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class AdminBrowseEventActivity extends AppCompatActivity {

    private ListView listView;
    private AdminGenericEventAdapter adapter;
    private List<Object> eventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_browse_event);

        findViewById(R.id.button_back2).setOnClickListener(v -> finish());

        listView = findViewById(R.id.event_list);
        eventList = new ArrayList<>();

        adapter = new AdminGenericEventAdapter(
                this,
                eventList,
                R.layout.admin_event_card
        );

        listView.setAdapter(adapter);

        loadEvents();
    }

    private void loadEvents() {

        EventController.getInstance().getAllEvents(new RepositoryCallback<List<Event>>() {

            @Override
            public void onSuccess(List<Event> result) {

                eventList.clear();
                eventList.addAll(result); // Event → Object 자동 업캐스팅

                runOnUiThread(() -> adapter.notifyDataSetChanged());
            }

            @Override
            public void onFailure(Exception e) {

                runOnUiThread(() ->
                        Toast.makeText(AdminBrowseEventActivity.this,
                                "Failed to load events",
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}