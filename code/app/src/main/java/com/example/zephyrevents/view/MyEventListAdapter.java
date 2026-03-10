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
import com.example.zephyrevents.model.EventStatus;
import com.example.zephyrevents.model.MyEventEntry;
import com.example.zephyrevents.model.Status;
import com.example.zephyrevents.model.WaitlistEntry;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Adapter for the My Events list (Lotteries and History tabs).
 * Shows event title, duration ("1 day ago" / "3 days ago"), and status badge
 * (SELECTED, WAITING, or NOT SELECTED).
 */
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
        if (entry == null) {
            return row;
        }

        TextView title = row.findViewById(R.id.my_event_title);
        TextView duration = row.findViewById(R.id.my_event_duration);
        TextView status = row.findViewById(R.id.my_event_status);

        if (entry.isPlaceholder()) {
            title.setText(entry.getPlaceholderTitle());
            duration.setText("");
            setStatusBadge(status, null, true);
        } else {
            // This is probably wrong, we probably want the title here not the id.
            Event event = EventController.getInstance().getEvent(entry.getEventId());
            title.setText(event != null ? event.getName() : entry.getEventId());
            duration.setText(formatDuration(entry.getTimestamp()));
            setStatusBadge(status, entry.getStatus(), false);
        }

        return row;
    }

    /**
     * Sets the status badge text and style (green SELECTED, yellow WAITING, red NOT SELECTED / DECLINED).
     */

    // not my cleanest enum switch tbh, I'll clean it up later hopefully.
    private void setStatusBadge(TextView statusView, Status userEventStatus, boolean notSelected){
        if (notSelected || userEventStatus == null){
            statusView.setText(R.string.status_not_selected);
            statusView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_badge_not_selected));
            statusView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            return;
        }
        switch (userEventStatus){
            case REMOVED:
                statusView.setText(R.string.status_declined);
                statusView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_badge_not_selected));
                statusView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                return;
            case DECLINED:
                statusView.setText(R.string.status_declined);
                statusView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_badge_not_selected));
                statusView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                return;
            case SELECTED:
                statusView.setText(R.string.status_selected);
                statusView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_badge_selected));
                statusView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            case ACCEPTED:
                statusView.setText(R.string.status_selected);
                statusView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_badge_selected));
                statusView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            case WAITLISTED:
                statusView.setText(R.string.status_waiting);
                statusView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_badge_waiting));
                statusView.setTextColor(ContextCompat.getColor(getContext(), R.color.black));

            default:
                statusView.setText(R.string.status_waiting);
                statusView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_badge_waiting));
                statusView.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        }
    }

    /**
     * Returns "1 day ago" or "X days ago" from joinedAtMillis to now.
     */
    private String formatDuration(long joinedAtMillis) {
        if (joinedAtMillis <= 0) return "";
        long diffMillis = System.currentTimeMillis() - joinedAtMillis;
        long days = TimeUnit.MILLISECONDS.toDays(diffMillis);
        if (days <= 0 || days == 1) return getContext().getString(R.string.one_day_ago);
        return getContext().getString(R.string.days_ago, (int) days);
    }
}
