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
import com.example.zephyrevents.controller.UserController;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.model.WaitlistEntry;
import com.example.zephyrevents.repository.RepositoryCallback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.widget.ImageView;
import com.bumptech.glide.Glide;


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

        final View rowFinal = row;

        WaitlistEntry entry = getItem(position);
        if (entry == null) return rowFinal;


        final String rowEventId = entry.getEventId();
        rowFinal.setTag(rowEventId);

        TextView titleView = row.findViewById(R.id.item_event_title);
        TextView dateLocationView = row.findViewById(R.id.item_event_date_location);
        TextView priceView = row.findViewById(R.id.item_event_price);
        TextView statusView = row.findViewById(R.id.item_event_status);
        ImageView eventPoster = row.findViewById(R.id.item_event_image);
        if (eventPoster != null){
            Glide.with(getContext()).clear(eventPoster);
            eventPoster.setImageResource(R.drawable.event_card_placeholder);
        }

        // Clear out old text while Firebase loads the actual event
        titleView.setText("Loading...");
        dateLocationView.setText("");
        priceView.setText("");
        statusView.setText("...");

        if (entry.getEventId() != null) {
            EventController.getInstance().getEventById(entry.getEventId(), new RepositoryCallback<Event>() {
                @Override
                public void onSuccess(Event result) {
                    if (result != null) {
                        if (rowEventId == null || result.getEventId() == null || !rowEventId.equals(result.getEventId())){
                            return;
                        }
                        Object tag = rowFinal.getTag();
                        if (tag == null || !tag.equals(result.getEventId())){
                            return;
                        }
                        titleView.setText(result.getName() != null ? result.getName() : "Unknown Event");

                        if(eventPoster != null){
                            String url = result.getImageUrl();
                            if(url != null && !url.isEmpty()){
                                Glide.with(getContext())
                                        .load(url)
                                        .centerCrop()
                                        .error(R.drawable.event_card_placeholder)
                                        .into(eventPoster);

                            } else {
                                eventPoster.setImageResource(R.drawable.event_card_placeholder);
                            }
                        }

                        if (result.getPrice() == 0.0) {
                            priceView.setText("Free");
                        } else {
                            priceView.setText(String.format(Locale.getDefault(), "$%.2f", result.getPrice()));
                        }

                        String dateStr = "";
                        if (result.getTime() != null && result.getTime().getStartTime() > 0) {
                            dateStr = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(new Date(result.getTime().getStartTime()));
                        }
                        String locStr = result.getLocation() != null && result.getLocation().getLocationString() != null ? result.getLocation().getLocationString() : "";

                        if (!dateStr.isEmpty() && !locStr.isEmpty()) {
                            dateLocationView.setText(dateStr + ", " + locStr);
                        } else {
                            dateLocationView.setText(dateStr + locStr);
                        }

                        // Evaluate Organizer vs Status
                        String currentUserId = new UserController(getContext()).getCurrentUserId();
                        boolean isOrganizer = currentUserId != null && result.getOrganizerId() != null && result.getOrganizerId().equals(currentUserId);

                        if (isOrganizer) {
                            statusView.setText("ORGANIZER");
                            statusView.setBackgroundResource(R.drawable.bg_badge_selected);
                            // Keep the rounded corners but tint it blue
                            statusView.getBackground().mutate().setTint(android.graphics.Color.parseColor("#2196F3"));
                            statusView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));

                        } else if (entry.getStatus() != null) {
                            switch (entry.getStatus()) {
                                case ACCEPTED:
                                    statusView.setText("ACCEPTED");
                                    statusView.setBackgroundResource(R.drawable.bg_badge_selected);
                                    statusView.getBackground().mutate().setTintList(null); // Clear recycled tints
                                    statusView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                                    break;

                                case DECLINED:
                                    statusView.setText("DECLINED");
                                    statusView.setBackgroundResource(R.drawable.bg_badge_selected);
                                    // Tint the rounded badge red
                                    statusView.getBackground().mutate().setTint(ContextCompat.getColor(getContext(), R.color.invite_declined_red));
                                    statusView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                                    break;

                                case SELECTED:
                                    statusView.setText("SELECTED");
                                    statusView.setBackgroundResource(R.drawable.bg_badge_selected);
                                    statusView.getBackground().mutate().setTintList(null);
                                    statusView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                                    break;

                                case LOST:
                                    statusView.setText("NOT SELECTED");
                                    statusView.setBackgroundResource(R.drawable.bg_badge_selected);
                                    // Tint the rounded badge grey
                                    statusView.getBackground().mutate().setTint(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
                                    statusView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                                    break;

                                case WAITLISTED:
                                default:
                                    statusView.setText("WAITING");
                                    statusView.setBackgroundResource(R.drawable.bg_badge_waiting);
                                    statusView.getBackground().mutate().setTintList(null);
                                    statusView.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                                    break;
                            }
                        }
                    }
                }
                @Override
                public void onFailure(Exception e) {
                    titleView.setText("Unknown Event");
                }
            });
        }

        return rowFinal;
    }}