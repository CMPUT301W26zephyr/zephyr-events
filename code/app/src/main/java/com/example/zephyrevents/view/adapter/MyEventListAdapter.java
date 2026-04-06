package com.example.zephyrevents.view.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

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
        if (eventPoster != null) {
            Glide.with(getContext()).clear(eventPoster);
            eventPoster.setImageResource(R.drawable.event_card_placeholder);
        }

        applyTicketLoadingStyle(row);

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
                        if (rowEventId == null || result.getEventId() == null || !rowEventId.equals(result.getEventId())) {
                            return;
                        }
                        Object tag = rowFinal.getTag();
                        if (tag == null || !tag.equals(result.getEventId())) {
                            return;
                        }
                        titleView.setText(result.getName() != null ? result.getName() : "Unknown Event");

                        if (eventPoster != null) {
                            String url = result.getImageUrl();
                            if (url != null && !url.isEmpty()) {
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
                            dateStr = new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.getDefault()).format(new Date(result.getTime().getStartTime()));
                        }
                        String locStr = result.getLocation() != null && result.getLocation().getLocationString() != null ? result.getLocation().getLocationString() : "";

                        if (!dateStr.isEmpty() && !locStr.isEmpty()) {
                            dateLocationView.setText(dateStr + "\n" + locStr);
                        } else {
                            dateLocationView.setText(dateStr + locStr);
                        }

                        String currentUserId = new UserController(getContext()).getCurrentUserId();
                        boolean isOrganizer = currentUserId != null && result.getOrganizerId() != null && result.getOrganizerId().equals(currentUserId);
                        boolean isCoOrganizer = currentUserId != null
                                && result.getCoOrganizerUserIds() != null
                                && result.getCoOrganizerUserIds().contains(currentUserId);

                        if (isOrganizer || isCoOrganizer) {
                            applyTicketColors(rowFinal, TicketKind.ORGANIZER);
                            statusView.setText("ORGANIZER");
                            statusView.setBackgroundResource(R.drawable.bg_badge_selected);
                            statusView.getBackground().mutate().setTint(android.graphics.Color.parseColor("#2196F3"));
                            statusView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));

                        } else if (entry.getStatus() != null) {
                            switch (entry.getStatus()) {
                                case ACCEPTED:
                                    applyTicketColors(rowFinal, TicketKind.ACCEPTED);
                                    statusView.setText("ACCEPTED");
                                    statusView.setBackgroundResource(R.drawable.bg_badge_selected);
                                    statusView.getBackground().mutate().setTintList(null);
                                    statusView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                                    break;

                                case DECLINED:
                                    applyTicketColors(rowFinal, TicketKind.DECLINED);
                                    statusView.setText("DECLINED");
                                    statusView.setBackgroundResource(R.drawable.bg_badge_selected);
                                    statusView.getBackground().mutate().setTint(ContextCompat.getColor(getContext(), R.color.invite_declined_red));
                                    statusView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                                    break;

                                case SELECTED:
                                    applyTicketColors(rowFinal, TicketKind.SELECTED);
                                    statusView.setText("SELECTED");
                                    statusView.setBackgroundResource(R.drawable.bg_badge_selected);
                                    statusView.getBackground().mutate().setTintList(null);
                                    statusView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                                    break;

                                case LOST:
                                    applyTicketColors(rowFinal, TicketKind.LOST);
                                    statusView.setText("NOT SELECTED");
                                    statusView.setBackgroundResource(R.drawable.bg_badge_selected);
                                    statusView.getBackground().mutate().setTint(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
                                    statusView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                                    break;

                                case WAITLISTED:
                                default:
                                    applyTicketColors(rowFinal, TicketKind.WAITING);
                                    statusView.setText("WAITING");
                                    statusView.setBackgroundResource(R.drawable.bg_badge_waiting);
                                    statusView.getBackground().mutate().setTintList(null);
                                    statusView.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                                    break;
                            }
                        } else {
                            applyTicketColors(rowFinal, TicketKind.NEUTRAL);
                        }
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    rowFinal.setVisibility(View.GONE);
                    rowFinal.setLayoutParams(new android.widget.AbsListView.LayoutParams(-1, 1));
                }
            });
        }

        return rowFinal;
    }

    private void applyTicketLoadingStyle(View row) {
        applyTicketColors(row, TicketKind.LOADING);
    }

    private enum TicketKind {
        LOADING,
        NEUTRAL,
        ORGANIZER,
        WAITING,
        SELECTED,
        ACCEPTED,
        DECLINED,
        LOST
    }

    private void applyTicketColors(@NonNull View row, @NonNull TicketKind kind) {
        Context ctx = getContext();
        MaterialCardView card = row.findViewById(R.id.ticket_card_root);
        View stripe = row.findViewById(R.id.ticket_accent_stripe);
        if (card == null || stripe == null) {
            return;
        }

        int bg;
        int stripeColor;

        switch (kind) {
            case LOADING:
                bg = ContextCompat.getColor(ctx, R.color.ticket_bg_loading);
                stripeColor = ContextCompat.getColor(ctx, R.color.light_stroke);
                break;
            case NEUTRAL:
                bg = ContextCompat.getColor(ctx, R.color.ticket_bg_loading);
                stripeColor = ContextCompat.getColor(ctx, R.color.text_secondary);
                break;
            case ORGANIZER:
                bg = ContextCompat.getColor(ctx, R.color.ticket_bg_organizer);
                stripeColor = ContextCompat.getColor(ctx, R.color.ticket_stripe_organizer);
                break;
            case WAITING:
                bg = ContextCompat.getColor(ctx, R.color.ticket_bg_waiting);
                stripeColor = ContextCompat.getColor(ctx, R.color.ticket_stripe_waiting);
                break;
            case SELECTED:
                bg = ContextCompat.getColor(ctx, R.color.ticket_bg_selected);
                stripeColor = ContextCompat.getColor(ctx, R.color.ticket_stripe_selected);
                break;
            case ACCEPTED:
                bg = ContextCompat.getColor(ctx, R.color.ticket_bg_accepted);
                stripeColor = ContextCompat.getColor(ctx, R.color.ticket_stripe_accepted);
                break;
            case DECLINED:
                bg = ContextCompat.getColor(ctx, R.color.ticket_bg_declined);
                stripeColor = ContextCompat.getColor(ctx, R.color.ticket_stripe_declined);
                break;
            case LOST:
                bg = ContextCompat.getColor(ctx, R.color.ticket_bg_lost);
                stripeColor = ContextCompat.getColor(ctx, R.color.ticket_stripe_lost);
                break;
            default:
                bg = ContextCompat.getColor(ctx, R.color.ticket_bg_loading);
                stripeColor = ContextCompat.getColor(ctx, R.color.ticket_stripe_waiting);
                break;
        }

        card.setCardBackgroundColor(bg);
        stripe.setBackgroundColor(stripeColor);
        card.setStrokeColor(ColorStateList.valueOf(stripeColor));
    }
}
