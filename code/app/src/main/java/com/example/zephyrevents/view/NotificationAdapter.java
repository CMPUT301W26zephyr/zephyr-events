package com.example.zephyrevents.view;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zephyrevents.R;
import com.example.zephyrevents.model.Notification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notifications;

    public NotificationAdapter(List<Notification> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notif = notifications.get(position);

        String title = "Notification";
        if (notif.getType() != null) {
            String typeStr = notif.getType().name().replace("_", " ").toLowerCase();
            title = typeStr.substring(0, 1).toUpperCase() + typeStr.substring(1);
        }
        holder.titleText.setText(title);

        holder.descText.setText(notif.getText());

        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(notif.getTime(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
        holder.timeText.setText(timeAgo);

        holder.unreadIndicator.setVisibility(notif.isRead() ? View.GONE : View.VISIBLE);

        holder.btnGoto.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Going to event" + notif.getEventId(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, descText, timeText;
        View unreadIndicator;
        Button btnGoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.notification_title);
            descText = itemView.findViewById(R.id.notification_desc);
            timeText = itemView.findViewById(R.id.notification_time);
            unreadIndicator = itemView.findViewById(R.id.indicator_unread);
            btnGoto = itemView.findViewById(R.id.btn_goto_event);
        }
    }
}