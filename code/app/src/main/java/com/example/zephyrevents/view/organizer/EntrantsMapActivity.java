package com.example.zephyrevents.view.organizer;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zephyrevents.R;
import com.example.zephyrevents.model.WaitlistEntry;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.repository.WaitlistRepository;
import com.example.zephyrevents.view.event.EventDetailViewActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;
// Map of entrant/check-in locations for an event; map-centric activity separates geospatial UI from the flat entrant list.
// The following class is from Anthropic, Claude (claude.ai), "Android map activity showing waitlist entrant locations using osmdroid", 2025-03-30
public class EntrantsMapActivity extends AppCompatActivity {

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_entrants_map);

        String eventId = getIntent().getStringExtra(EventDetailViewActivity.EXTRA_EVENT);

        findViewById(R.id.btn_cancel).setVisibility(View.GONE);
        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());
        TextView title = findViewById(R.id.toolbar_title);
        title.setText("Entrant Locations");

        mapView = findViewById(R.id.map_view);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(10.0);

        new WaitlistRepository().getWaitlist(eventId, new RepositoryCallback<List<WaitlistEntry>>() {
            @Override
            public void onSuccess(List<WaitlistEntry> entries) {
                if (entries == null || entries.isEmpty()) {
                    Toast.makeText(EntrantsMapActivity.this, "No entrants yet.", Toast.LENGTH_SHORT).show();
                    return;
                }

                GeoPoint first = null;

                for (WaitlistEntry entry : entries) {
                    if (entry.getCoordinates() == null) continue;
                    double lat = entry.getCoordinates().getLat();
                    double lng = entry.getCoordinates().getLng();
                    if (lat == 0 && lng == 0) continue;

                    GeoPoint point = new GeoPoint(lat, lng);
                    Marker marker = new Marker(mapView);
                    marker.setPosition(point);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    marker.setTitle(entry.getUserId());

                    android.graphics.drawable.GradientDrawable circle = new android.graphics.drawable.GradientDrawable();
                    circle.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                    circle.setColor(androidx.core.content.ContextCompat.getColor(EntrantsMapActivity.this, R.color.primary_red));
                    circle.setSize(40, 40);
                    marker.setIcon(circle);

                    mapView.getOverlays().add(marker);

                    if (first == null) first = point;
                }

                if (first != null) {
                    mapView.getController().setCenter(first);
                }

                mapView.invalidate();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EntrantsMapActivity.this, "Failed to load entrants.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() { super.onResume(); mapView.onResume(); }

    @Override
    protected void onPause() { super.onPause(); mapView.onPause(); }
}
