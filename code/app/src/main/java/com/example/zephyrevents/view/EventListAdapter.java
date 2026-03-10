package com.example.zephyrevents.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.zephyrevents.R;
import com.example.zephyrevents.model.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter that fills the "All Events" list with event cards.
 * Each row shows the event image, title, date and location, and price on the right.
 */
public class EventListAdapter extends ArrayAdapter<Event> {

    private final LayoutInflater inflater;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    public EventListAdapter(@NonNull Context context, @NonNull List<Event> events) {
        super(context, R.layout.item_event_card, events);
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            row = inflater.inflate(R.layout.item_event_card, parent, false);
        }

        Event event = getItem(position);
        if (event == null) {
            return row;
        }

        ImageView image = row.findViewById(R.id.item_event_image);
        TextView title = row.findViewById(R.id.item_event_title);
        TextView dateLocation = row.findViewById(R.id.item_event_date_location);
        TextView price = row.findViewById(R.id.item_event_price);

        title.setText(event.getName());
        String dateStr = event.getStartTime() > 0 ? dateFormat.format(new Date(event.getStartTime())) : "";
        String locationStr = event.getLocation() != null ? event.getLocation() : "";
        String dateLocationStr;
        if (dateStr.isEmpty() && locationStr.isEmpty()) {
            dateLocationStr = getContext().getString(R.string.date_location);
        } else {
            dateLocationStr = dateStr + (locationStr.isEmpty() ? "" : ", " + locationStr);
        }
        dateLocation.setText(dateLocationStr);
        price.setText(event.getPrice() != null ? event.getPrice() : "");

        return row;
    }
}
