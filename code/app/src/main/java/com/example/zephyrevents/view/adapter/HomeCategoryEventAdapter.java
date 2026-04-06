package com.example.zephyrevents.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.zephyrevents.R;
import com.example.zephyrevents.model.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Compact horizontal cards for home explore category rows (below featured carousel).
 */
public class HomeCategoryEventAdapter extends RecyclerView.Adapter<HomeCategoryEventAdapter.Holder> {

    private final List<Event> events;
    private final OnEventClickListener listener;
    private static final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public HomeCategoryEventAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_category_event, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Event event = events.get(position);
        holder.title.setText(event.getName() != null ? event.getName() : "Unnamed Event");

        long startMs = event.getTime() != null ? event.getTime().getStartTime() : 0L;
        holder.date.setText(startMs > 0 ? dateFormat.format(new Date(startMs)) : "Date TBD");

        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(event.getImageUrl())
                    .placeholder(R.drawable.event_card_placeholder)
                    .centerCrop()
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.event_card_placeholder);
        }

        holder.itemView.setOnClickListener(v -> listener.onEventClick(event));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView title;
        final TextView date;

        Holder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.category_event_image);
            title = itemView.findViewById(R.id.category_event_title);
            date = itemView.findViewById(R.id.category_event_date);
        }
    }
}
