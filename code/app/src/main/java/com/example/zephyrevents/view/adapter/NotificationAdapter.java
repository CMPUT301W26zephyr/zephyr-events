package com.example.zephyrevents.view.adapter;

import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zephyrevents.R;
import com.example.zephyrevents.model.Notification;
import com.example.zephyrevents.model.NotificationType;
import com.example.zephyrevents.view.event.EventDetailViewActivity;

import java.util.List;

// List notifications for the users and routes taps into the event/welcome flow
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

        // 1. Bind the explicit Mockup Titles
        String title;
        if (type == NotificationType.WON_EVENT) {
            title = "You've been Selected!";
        } else if (type == NotificationType.LOST_EVENT) {
            title = "Lottery Results";
        } else if (type == NotificationType.LOTTERY_COMPLETED) {
            title = "Lottery Complete";
        } else if (type == NotificationType.PRIVATE_EVENT_INVITE) {
            title = "You've been Invited";
        } else if (type == NotificationType.CO_ORGANIZER_INVITE) {
            title = "Co-Organizer Invite";
        } else if (type == NotificationType.MANUAL) {
            title = "Organizer Update";
        } else {
            title = "Notification";
        }
        holder.titleText.setText(title);

        // 2. Bind the description
        holder.descText.setText(notif.getText() != null ? notif.getText() : "");

        // 3. Format Time
        long when = notif.getTime();
        if (when <= 0) when = System.currentTimeMillis();
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(when, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
        holder.timeText.setText(timeAgo.toString());

        // 4. Event Image
        com.bumptech.glide.Glide.with(holder.itemView.getContext()).clear(holder.avatarImg);
        holder.avatarImg.setImageResource(R.drawable.ic_image_placeholder2);

        // Fetch Event image directly
        if (notif.getEventId() != null && !notif.getEventId().isEmpty()) {
            com.example.zephyrevents.controller.EventController.getInstance().getEventById(notif.getEventId(), new com.example.zephyrevents.repository.RepositoryCallback<com.example.zephyrevents.model.Event>() {
                @Override
                public void onSuccess(com.example.zephyrevents.model.Event e) {
                    if (e != null && e.getImageUrl() != null && !e.getImageUrl().isEmpty()) {
                        com.bumptech.glide.Glide.with(holder.itemView.getContext())
                                .load(e.getImageUrl())
                                .centerCrop()
                                .into(holder.avatarImg);
                    }
                }
                @Override public void onFailure(Exception ex) {}
            });
        }

        // 5. Button Visibility logic (Losers don't get a button)
        holder.btnGoto.setVisibility(View.VISIBLE);
        holder.btnGoto.setText("View Event");

        // Button Action
        holder.btnGoto.setOnClickListener(v -> {
            if (notif.getEventId() == null || notif.getEventId().isEmpty()) {
                Toast.makeText(v.getContext(), "No event linked to this notification.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(v.getContext(), EventDetailViewActivity.class);
            intent.putExtra(EventDetailViewActivity.EXTRA_EVENT, notif.getEventId());
            v.getContext().startActivity(intent);
        });

        holder.moreIcon.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(v.getContext(), holder.moreIcon);
            popup.getMenu().add("Delete");
            popup.setOnMenuItemClickListener(item -> {

                new com.google.android.material.dialog.MaterialAlertDialogBuilder(v.getContext())
                        .setTitle("Delete Notification")
                        .setMessage("Are you sure you want to delete this notification?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            int safePosition = holder.getBindingAdapterPosition();
                            if (safePosition != RecyclerView.NO_POSITION) {
                                new com.example.zephyrevents.repository.NotificationRepository()
                                        .deleteNotification(notif.getNotificationId(), new com.example.zephyrevents.repository.RepositoryCallback<Void>() {
                                            @Override
                                            public void onSuccess(Void result) {
                                                notifications.remove(safePosition);
                                                notifyItemRemoved(safePosition);
                                                notifyItemRangeChanged(safePosition, notifications.size());
                                            }
                                            @Override
                                            public void onFailure(Exception e) { }
                                        });
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();

                return true;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, descText, timeText;
        ImageView avatarImg, moreIcon;
        Button btnGoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImg = itemView.findViewById(R.id.notification_avatar);
            titleText = itemView.findViewById(R.id.notification_title);
            descText = itemView.findViewById(R.id.notification_desc);
            timeText = itemView.findViewById(R.id.notification_time);
            btnGoto = itemView.findViewById(R.id.btn_goto_event);
            moreIcon = itemView.findViewById(R.id.notification_more);
        }
    }
}