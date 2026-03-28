package com.example.zephyrevents.view;

import android.content.Intent;
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
import com.example.zephyrevents.model.NotificationType;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<Notification> notifications;

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
        NotificationType type = notif.getType();

        String title;
        if (type == NotificationType.PRIVATE_EVENT_INVITE) {
            title = holder.itemView.getContext().getString(R.string.notif_private_invite_title);
        } else if (type == NotificationType.CO_ORGANIZER_INVITE) {
            title = holder.itemView.getContext().getString(R.string.notif_coorganizer_title);
        } else if (type != null) {
            String typeStr = type.name().replace("_", " ").toLowerCase();
            title = typeStr.substring(0, 1).toUpperCase() + typeStr.substring(1);
        } else {
            title = holder.itemView.getContext().getString(R.string.notifications);
        }
        holder.titleText.setText(title);

        holder.descText.setText(notif.getText() != null ? notif.getText() : "");

        long when = notif.getTime();
        if (when <= 0) {
            when = System.currentTimeMillis();
        }
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(when, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
        holder.timeText.setText(timeAgo);

        holder.unreadIndicator.setVisibility(notif.isRead() ? View.GONE : View.VISIBLE);

        boolean isInviteType = type == NotificationType.PRIVATE_EVENT_INVITE
                || type == NotificationType.CO_ORGANIZER_INVITE;
        holder.btnGoto.setText(isInviteType ? R.string.cta_view_invite : R.string.cta_go_to_event);

        holder.btnGoto.setOnClickListener(v -> {
            if (type == NotificationType.CO_ORGANIZER_INVITE) {
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                intent.putExtra("TARGET_TAB", "MyEvents");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                v.getContext().startActivity(intent);
                return;
            }
            if (notif.getEventId() == null || notif.getEventId().isEmpty()) {
                Toast.makeText(v.getContext(), "No event linked to this notification.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(v.getContext(), EventDetailViewActivity.class);
            intent.putExtra(EventDetailViewActivity.EXTRA_EVENT, notif.getEventId());
            v.getContext().startActivity(intent);
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
