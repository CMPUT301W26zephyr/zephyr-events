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

public class FeaturedEventPagerAdapter extends RecyclerView.Adapter<FeaturedEventPagerAdapter.EventViewHolder> {

    private final List<Event> eventList;
    private final OnEventClickListener listener;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.getDefault());

    /**
     * Interface required for onEventClick
     */
    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public FeaturedEventPagerAdapter(List<Event> eventList, OnEventClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_card_featured, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView imageLarge;
        TextView textTitle, textDate, textLocation, textPrice, textDescription;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            imageLarge = itemView.findViewById(R.id.item_event_image_large);
            textTitle = itemView.findViewById(R.id.item_event_title);
            textDate = itemView.findViewById(R.id.event_date);
            textLocation = itemView.findViewById(R.id.event_location);
            textPrice = itemView.findViewById(R.id.event_price);
            textDescription = itemView.findViewById(R.id.event_description);
        }

        /**
         * Sets stuff in the "slide" upon loading
         * @param event
         * @param listener
         */
        void bind(Event event, OnEventClickListener listener) {
            textTitle.setText(event.getName() != null ? event.getName() : "Unnamed Event");

            String dateStr = event.getTime().getStartTime() > 0 ? dateFormat.format(new Date(event.getTime().getStartTime())) : "Date TBD";
            textDate.setText(dateStr);

            String locationStr = event.getLocation() != null ? event.getLocation().getLocationString() : "Location TBD";
            textLocation.setText(locationStr);

            if (event.getPrice() <= 0) {
                textPrice.setText("Free");
                textPrice.setTextColor(itemView.getContext().getResources().getColor(R.color.green));
            } else {
                textPrice.setText(String.format(Locale.getDefault(), "$%.2f", event.getPrice()));
            }

            if (event.getDescription() != null && !event.getDescription().trim().isEmpty()) {
                textDescription.setText(event.getDescription());
            } else {
                textDescription.setText("No Description...\nClick for details!");
            }

            if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(event.getImageUrl())
                        .placeholder(R.drawable.event_card_placeholder)
                        .into(imageLarge);
            } else {
                imageLarge.setImageResource(R.drawable.event_card_placeholder);
            }

            itemView.setOnClickListener(v -> listener.onEventClick(event));
        }
    }
}