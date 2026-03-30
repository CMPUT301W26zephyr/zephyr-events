package com.example.zephyrevents.view;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.model.User;
import com.example.zephyrevents.repository.RepositoryCallback;

import java.util.List;

public class AdminGenericEventAdapter extends ArrayAdapter<Object> {

    private List<Object> itemList;
    private int layoutRes;

    public AdminGenericEventAdapter(Context context, List<Object> items, int layoutRes) {
        super(context, 0, items);
        this.itemList = items;
        this.layoutRes = layoutRes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Object item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(layoutRes, parent, false);
        }

        // =========================
        // EVENT TYPE
        // =========================
        if (item instanceof Event) {

            Event event = (Event) item;

            TextView title = convertView.findViewById(R.id.event_title);
            TextView organizer = convertView.findViewById(R.id.event_organizer);
            TextView date = convertView.findViewById(R.id.event_date);
            View deleteBtn = convertView.findViewById(R.id.button_delete);

            if (title != null)
                title.setText(event.getName());

            if (organizer != null) {
                organizer.setText(
                        event.getOrganizerName() != null ?
                                event.getOrganizerName() : "Unknown Organizer"
                );
            }

            if (date != null) {
                date.setText(
                        event.getTime() != null ?
                                event.getTime().eventTimeLeft() : "No date"
                );
            }

            // DELETE 버튼
            if (deleteBtn != null) {
                deleteBtn.setOnClickListener(v -> {

                    new AlertDialog.Builder(getContext())
                            .setTitle("Delete Event")
                            .setMessage("Are you sure you want to delete this event?")
                            .setPositiveButton("Delete", (dialog, which) -> {

                                EventController.getInstance().deleteEvent(
                                        event.getEventId(),
                                        new RepositoryCallback<Void>() {

                                            @Override
                                            public void onSuccess(Void result) {
                                                itemList.remove(event);
                                                notifyDataSetChanged();
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                                // optional
                                            }
                                        }
                                );

                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });
            }
        }

        // =========================
        // USER TYPE
        // =========================
        else if (item instanceof User) {

            User user = (User) item;

            TextView name = convertView.findViewById(R.id.user_name);
            TextView email = convertView.findViewById(R.id.user_email);

            if (name != null) {
                name.setText(
                        user.getName() != null ?
                                user.getName() : "Unknown User"
                );
            }

            if (email != null && user.getContactInfo() != null) {
                email.setText(
                        user.getContactInfo().getEmail() != null ?
                                user.getContactInfo().getEmail() : "No Email"
                );
            }
        }

        // =========================
        // IMAGE TYPE
        // =========================
        else if (item instanceof String) {

            ImageView image = convertView.findViewById(R.id.image_view);

            if (image != null) {
                image.setImageResource(R.drawable._4property_1_placeholder_image);


                image.setOnClickListener(v -> {

                    new AlertDialog.Builder(getContext())
                            .setTitle("Info")
                            .setMessage("moving to event page")
                            .setPositiveButton("OK", null)
                            .show();

                });
            }
        }

        return convertView;
    }
}