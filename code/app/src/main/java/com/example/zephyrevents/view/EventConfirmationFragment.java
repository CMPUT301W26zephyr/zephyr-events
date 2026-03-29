package com.example.zephyrevents.view;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.model.EventTime;
import com.example.zephyrevents.model.EventViewModel;
import com.example.zephyrevents.repository.RepositoryCallback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class EventConfirmationFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_confirmation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EventViewModel viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        // Required Fields (Always Display)
        TextView title = view.findViewById(R.id.text_confirm_title);
        TextView date = view.findViewById(R.id.text_confirm_date);
        TextView regPeriod = view.findViewById(R.id.text_confirm_reg_period);
        TextView location = view.findViewById(R.id.text_confirm_location);
        TextView geo = view.findViewById(R.id.text_confirm_geo);
        TextView capacity = view.findViewById(R.id.text_confirm_capacity);
        TextView visibility = view.findViewById(R.id.text_confirm_visibility);
        visibility.setText(viewModel.privateEvent
                ? getString(R.string.visibility_private_label)
                : getString(R.string.visibility_public_label));

        title.setText(viewModel.title);
        date.setText(viewModel.eventDate);
        regPeriod.setText(viewModel.registrationPeriod);
        location.setText(viewModel.location);
        geo.setText(viewModel.requireGeolocation ? "Required" : "Not Required");

        if (viewModel.waitlistCapacity == null || viewModel.waitlistCapacity.isEmpty()) {
            capacity.setText(viewModel.attendeeCount + " attendees (No Waitlist)");
        } else {
            capacity.setText(viewModel.attendeeCount + " attendees (" + viewModel.waitlistCapacity + " waitlist)");
        }

        if (viewModel.type == null || viewModel.type.isEmpty()) {
            view.findViewById(R.id.row_type).setVisibility(View.GONE);
        } else {
            TextView type = view.findViewById(R.id.text_confirm_type);
            type.setText(viewModel.type);
        }
        if (viewModel.address == null || viewModel.address.isEmpty()) {
            view.findViewById(R.id.row_address).setVisibility(View.GONE);
        } else {
            TextView address = view.findViewById(R.id.text_confirm_address);
            address.setText(viewModel.address);
        }
        if (viewModel.price == null || viewModel.price.isEmpty()) {
            view.findViewById(R.id.row_price).setVisibility(View.GONE);
        } else {
            TextView price = view.findViewById(R.id.text_confirm_price);
            price.setText("$" + viewModel.price);
        }
        TextView description = view.findViewById(R.id.text_confirm_description);
        if (viewModel.description == null || viewModel.description.isEmpty()) {
            description.setText("No event description provided.");
            description.setTypeface(null, Typeface.ITALIC);
        } else {
            description.setText(viewModel.description);
        }

        ((OrganizerEventAddEditView) requireActivity()).setupTopAndBottomUI(
                "Review Event Details", "CONFIRM & CREATE", v -> {

                    // 1. Build the Event Object
                    Event newEvent = new Event();

                    // --- NEW: Check if Edit Mode! ---
                    if (viewModel.isEditMode) {
                        newEvent.setEventId(viewModel.eventId);
                        newEvent.setOrganizerId(viewModel.organizerId); // Preserve owner
                        newEvent.setCurrentApplicants(viewModel.originalApplicants); // Preserve users
                    } else {
                        newEvent.setEventId(java.util.UUID.randomUUID().toString());
                        newEvent.setOrganizerId(new com.example.zephyrevents.controller.UserController(requireContext()).getCurrentUserId());
                    }

                    newEvent.setName(viewModel.title);
                    newEvent.setDescription(viewModel.description);
                    // ... (Keep your existing Location and Date parsing code exactly the same here) ...
                    try { newEvent.setPrice(Double.parseDouble(viewModel.price)); } catch (Exception e) { newEvent.setPrice(0.0); }
                    try { newEvent.setCapacity(Integer.parseInt(viewModel.attendeeCount)); } catch (Exception e) { newEvent.setCapacity(0); }

                    // --- NEW: Map Waitlist Capacity ---
                    try {
                        if (viewModel.waitlistCapacity != null && !viewModel.waitlistCapacity.trim().isEmpty()) {
                            newEvent.setWaitlistCapacity(Integer.parseInt(viewModel.waitlistCapacity.trim()));
                        } else {
                            newEvent.setWaitlistCapacity(null);
                        }
                    } catch (Exception e) {
                        newEvent.setWaitlistCapacity(null);
                    }

                    // --- NEW: Combine Location & Address and attach it to the Event ---
                    com.example.zephyrevents.model.Location eventLoc = new com.example.zephyrevents.model.Location();
                    String displayLocation = viewModel.location;
                    if (viewModel.address != null && !viewModel.address.trim().isEmpty()) {
                        displayLocation += " (" + viewModel.address.trim() + ")";
                    }
                    // Assuming your Location model has a standard setter
                    eventLoc.setLocationString(displayLocation);
                    newEvent.setLocation(eventLoc);

                    // Parse the dates into EventTime format using the FULL formatter including time
                    SimpleDateFormat sdfFull = new SimpleDateFormat("MMM d, yyyy, h:mm a", java.util.Locale.getDefault());
                    SimpleDateFormat sdfDateOnly = new SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault());

                    try {
                        if (viewModel.eventDate != null && !viewModel.eventDate.isEmpty()) {
                            Date eDate = null;
                            try {
                                eDate = sdfFull.parse(viewModel.eventDate);
                            } catch (ParseException e) {
                                eDate = sdfDateOnly.parse(viewModel.eventDate); // Fallback for old data
                            }
                            if (eDate != null) {
                                com.example.zephyrevents.model.EventTime time = new com.example.zephyrevents.model.EventTime(eDate.getTime(), eDate.getTime() + 7200000);
                                newEvent.setTime(time);
                            }
                        }
                        if (viewModel.registrationPeriod != null && viewModel.registrationPeriod.contains(" - ")) {
                            String[] parts = viewModel.registrationPeriod.split(" - ");
                            Date regEnd = null;
                            try {
                                regEnd = sdfFull.parse(parts[1]);
                            } catch (ParseException e) {
                                regEnd = sdfDateOnly.parse(parts[1]); // Fallback for old data
                            }
                            if (regEnd != null) newEvent.setRegistrationEndTime(regEnd.getTime());
                        }
                    } catch (Exception e) { e.printStackTrace(); }

                    newEvent.setPrivateEvent(viewModel.privateEvent);
                    newEvent.setCoOrganizerUserIds(new ArrayList<>(viewModel.coOrganizerUserIds));
                    newEvent.setPendingPrivateWaitlistInviteUserIds(new ArrayList<>(viewModel.pendingPrivateWaitlistInviteUserIds));

                    // 2. Save to Firebase
                    EventController.getInstance().createEvent(newEvent, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(requireContext(), "Event Created Successfully!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(requireContext(), "Failed to create event", Toast.LENGTH_SHORT).show();
                        }
                    });

                    // 3. CLOSE IMMEDIATELY (Optimistic UI)
                    // We don't wait for the network. We close the form instantly so the user isn't stuck waiting.
                    requireActivity().finish();
                }
        );
    }
}