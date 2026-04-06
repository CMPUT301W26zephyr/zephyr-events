package com.example.zephyrevents.view.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.zephyrevents.view.event.EventDetailViewActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bumptech.glide.Glide;
import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.util.DialogUiHelper;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.repository.RepositoryCallback;

import java.util.List;

// Lets admins browse and moderate stored images (e.g. posters/avatars); separates image review from regular event browsing.

public class AdminBrowseImageActivity extends AppCompatActivity {

    private GridLayout gridLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_browse_image);

        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        TextView title = findViewById(R.id.toolbar_title);
        if (title != null) title.setText("Browse Images");
        findViewById(R.id.btn_cancel).setVisibility(View.GONE);
        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());

        gridLayout = findViewById(R.id.image_grid_preview);

        setupGrid();
        loadEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }

    // Set 3 columns grid
    private void setupGrid() {
        gridLayout.setColumnCount(3);
    }

    // Load events from Firebase
    private void loadEvents() {

        EventController.getInstance().getAllEvents(new RepositoryCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {

                runOnUiThread(() -> {
                    gridLayout.removeAllViews();

                    for (Event event : events) {
                        if (!event.hasPosterImage()){
                            continue;
                        }

                        ImageView imageView = new ImageView(AdminBrowseImageActivity.this);

                        // Layout params
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.width = 0;
                        params.height = 350;
                        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                        params.setMargins(8, 8, 8, 8);
                        imageView.setLayoutParams(params);

                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                        // Load image from Firebase URL
                        Glide.with(AdminBrowseImageActivity.this)
                                .load(event.getImageUrl())
                                .centerCrop()
                                .into(imageView);


                        // Click → open detail (admin mode)
                        imageView.setOnClickListener(v -> {
                            openEventDetail(event);
                        });

                        gridLayout.addView(imageView);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(AdminBrowseImageActivity.this,
                                "Failed to load events", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    // Open event detail in admin mode
    private void openEventDetail(Event event) {

        Intent intent = new Intent(this, EventDetailViewActivity.class);
        intent.putExtra(EventDetailViewActivity.EXTRA_EVENT, event.getEventId());
        intent.putExtra("isAdminView", true);

        startActivity(intent);
        setupAdminDeleteHook(event);
    }

    // Admin delete logic (triggered from image click inside detail)
    private void setupAdminDeleteHook(Event event) {

        // This works because detail uses same activity lifecycle
        getWindow().getDecorView().postDelayed(() -> {

            View image = findViewById(R.id.event_image);

            if (image == null) return;

            image.setOnClickListener(v -> showDeleteDialog(event));

        }, 500);
    }

    // Show delete confirmation dialog
    private void showDeleteDialog(Event event) {

        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.admin_delete_yesorno, null);
        DialogUiHelper.bindAdminDeleteContent(dialogView,
                R.string.admin_delete_title_event,
                R.string.admin_delete_message_event);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialogView.findViewById(R.id.button_cancel)
                .setOnClickListener(v -> dialog.dismiss());

        // Delete event
        dialogView.findViewById(R.id.button_delete)
                .setOnClickListener(v -> {
                    dialog.dismiss();

                    EventController.getInstance().deleteEvent(
                            event.getEventId(),
                            new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(AdminBrowseImageActivity.this,
                                                "Event deleted", Toast.LENGTH_SHORT).show();

                                        loadEvents(); // refresh grid
                                    });
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    runOnUiThread(() ->
                                            Toast.makeText(AdminBrowseImageActivity.this,
                                                    "Delete failed", Toast.LENGTH_SHORT).show()
                                    );
                                }
                            }
                    );
                });

        dialog.show();
    }
}