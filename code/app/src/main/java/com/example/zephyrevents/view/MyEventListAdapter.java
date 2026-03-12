package com.example.zephyrevents.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.model.WaitlistEntry;
import com.example.zephyrevents.repository.RepositoryCallback;

import java.util.List;

public class MyEventListAdapter extends ArrayAdapter<WaitlistEntry> {

    private final LayoutInflater inflater;

    public MyEventListAdapter(@NonNull Context context, @NonNull List<WaitlistEntry> entries) {
        super(context, R.layout.item_my_event_card, entries);
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            row = inflater.inflate(R.layout.item_my_event_card, parent, false);
        }

        WaitlistEntry entry = getItem(position);
        if (entry == null) return row;

        TextView titleView = row.findViewById(R.id.my_event_title);
        TextView durationView = row.findViewById(R.id.my_event_duration);
        TextView statusView = row.findViewById(R.id.my_event_status);

        // Fetch Event Title Asynchronously from Firebase
        titleView.setText("Loading...");
        if (entry.getEventId() != null) {
            EventController.getInstance().getEventById(entry.getEventId(), new RepositoryCallback<Event>() {
                @Override
                public void onSuccess(Event result) {
                    if (result != null && result.getName() != null) {
                        titleView.setText(result.getName());
                    } else {
                        titleView.setText("Unknown Event");
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    titleView.setText("Unknown Event");
                }
            });
        }

        // Placeholder for duration until Waitlist timestamps are implemented
        durationView.setText("");

        // Set Status Badge
        if (entry.getStatus() != null) {
            switch (entry.getStatus()) {
                case DECLINED:
                    statusView.setText("NOT SELECTED"); // Using hardcoded text to prevent string resource errors
                    statusView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_badge_selected));
                    statusView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                    break;
                case ACCEPTED:
                    statusView.setText(R.string.status_selected);
                    statusView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_badge_selected));
                    statusView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                    break;
                case WAITLISTED:
                default:
                    statusView.setText(R.string.status_waiting);
                    statusView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_badge_waiting));
                    statusView.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                    break;
            }
        }

        return row;
    }
}