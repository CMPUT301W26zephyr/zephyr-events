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

import android.widget.ImageView;
import com.bumptech.glide.Glide;
import android.widget.Button;




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

        // Required Fields
        TextView title = view.findViewById(R.id.text_confirm_title);
        TextView date = view.findViewById(R.id.text_confirm_date);
        TextView regPeriod = view.findViewById(R.id.text_confirm_reg_period);
        TextView location = view.findViewById(R.id.text_confirm_location);
        TextView geo = view.findViewById(R.id.text_confirm_geo);
        TextView capacity = view.findViewById(R.id.text_confirm_capacity);
        TextView waitlistCap = view.findViewById(R.id.text_confirm_waitlist_capacity);
        TextView visibility = view.findViewById(R.id.text_confirm_visibility);
        TextView price = view.findViewById(R.id.text_confirm_price);

        if (viewModel.privateEvent) {
            visibility.setVisibility(View.VISIBLE);
        } else {
            visibility.setVisibility(View.GONE);
        }

        title.setText(viewModel.title);
        date.setText(viewModel.eventDate);
        regPeriod.setText(viewModel.registrationPeriod);
        geo.setText(viewModel.requireGeolocation ? "Required" : "Not Required");

        String locationStr = viewModel.location;
        if (viewModel.address != null && !viewModel.address.trim().isEmpty()) {
            locationStr += " (" + viewModel.address.trim() + ")";
        }
        location.setText(locationStr);

        // Format Capacities separately
        capacity.setText(viewModel.attendeeCount + " Attendees");
        if (viewModel.waitlistCapacity == null || viewModel.waitlistCapacity.trim().isEmpty()) {
            waitlistCap.setText("Unlimited");
        } else {
            waitlistCap.setText(viewModel.waitlistCapacity);
        }

        if (viewModel.price == null || viewModel.price.isEmpty() || viewModel.price.equals("0") || viewModel.price.equals("0.00")) {
            price.setText("Free");
        } else {
            price.setText("$" + viewModel.price);
        }

        TextView description = view.findViewById(R.id.text_confirm_description);
        if (viewModel.description == null || viewModel.description.isEmpty()) {
            description.setText("No event description provided.");
            description.setTypeface(null, Typeface.ITALIC);
        } else {
            description.setText(viewModel.description);
        }

        ImageView confirmImage = view.findViewById(R.id.confirm_event_image);
        if (viewModel.pendingEventImageUri != null){
            Glide.with(this).load(viewModel.pendingEventImageUri).centerCrop().into(confirmImage);
        } else if (viewModel.existingImgUrl != null && !viewModel.existingImgUrl.isEmpty()) {
            Glide.with(this).load(viewModel.existingImgUrl).centerCrop().into(confirmImage);
        } else{
            Glide.with(this).load(R.drawable.ic_image_placeholder2).centerCrop().into(confirmImage);
            confirmImage.setImageTintList(androidx.core.content.ContextCompat.getColorStateList(requireContext(), android.R.color.darker_gray));
        }

        String finalLocationStr = locationStr;
        ((OrganizerEventAddEditView) requireActivity()).setupTopAndBottomUI(
                "Review Event Details", "CONFIRM & CREATE", v -> {

                    Event newEvent = new Event();

                    if (viewModel.isEditMode) {
                        newEvent.setEventId(viewModel.eventId);
                        newEvent.setOrganizerId(viewModel.organizerId); // Preserve owner
                        newEvent.setCurrentApplicants(viewModel.originalApplicants); // Preserve users
                    } else {
                        if (viewModel.eventId == null || viewModel.eventId.isEmpty()) {
                            viewModel.eventId = java.util.UUID.randomUUID().toString();
                        }
                        newEvent.setEventId(viewModel.eventId);
                        newEvent.setOrganizerId(new com.example.zephyrevents.controller.UserController(requireContext()).getCurrentUserId());
                    }

                    newEvent.setName(viewModel.title);
                    newEvent.setDescription(viewModel.description);

                    try {
                        newEvent.setPrice(Double.parseDouble(viewModel.price));
                    } catch (Exception e) {
                        newEvent.setPrice(0.0);
                    }
                    try {
                        newEvent.setCapacity(Integer.parseInt(viewModel.attendeeCount));
                    } catch (Exception e) {
                        newEvent.setCapacity(0);
                    }

                    try {
                        if (viewModel.waitlistCapacity != null && !viewModel.waitlistCapacity.trim().isEmpty()) {
                            newEvent.setWaitlistCapacity(Integer.parseInt(viewModel.waitlistCapacity.trim()));
                        } else {
                            newEvent.setWaitlistCapacity(null);
                        }
                    } catch (Exception e) {
                        newEvent.setWaitlistCapacity(null);
                    }

                    com.example.zephyrevents.model.Location eventLoc = new com.example.zephyrevents.model.Location();
                    eventLoc.setLocationString(finalLocationStr);
                    newEvent.setLocation(eventLoc);

                    SimpleDateFormat sdfFull = new SimpleDateFormat("MMM d, yyyy, h:mm a", java.util.Locale.getDefault());
                    SimpleDateFormat sdfDateOnly = new SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault());

                    try {
                        if (viewModel.eventDate != null && !viewModel.eventDate.isEmpty()) {
                            Date eDate = null;
                            try {
                                eDate = sdfFull.parse(viewModel.eventDate.trim());
                            } catch (ParseException e) {
                                eDate = sdfDateOnly.parse(viewModel.eventDate.trim());
                            }
                            if (eDate != null) {
                                com.example.zephyrevents.model.EventTime time = new com.example.zephyrevents.model.EventTime(eDate.getTime(), eDate.getTime() + 7200000);
                                newEvent.setTime(time);
                            }
                        }
                        if (viewModel.registrationPeriod != null && viewModel.registrationPeriod.contains(" - ")) {
                            String[] parts = viewModel.registrationPeriod.split(" - ");
                            Date regStart = null, regEnd = null;
                            try {
                                regStart = sdfFull.parse(parts[0].trim());
                            } catch (ParseException e) {
                                regStart = sdfDateOnly.parse(parts[0].trim());
                            }
                            try {
                                regEnd = sdfFull.parse(parts[1].trim());
                            } catch (ParseException e) {
                                regEnd = sdfDateOnly.parse(parts[1].trim());
                            }

                            if (regStart != null)
                                newEvent.setRegistrationStartTime(regStart.getTime());
                            if (regEnd != null) newEvent.setRegistrationEndTime(regEnd.getTime());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    newEvent.setPrivateEvent(viewModel.privateEvent);
                    newEvent.setCoOrganizerUserIds(new ArrayList<>(viewModel.coOrganizerUserIds));
                    newEvent.setPendingPrivateWaitlistInviteUserIds(new ArrayList<>(viewModel.pendingPrivateWaitlistInviteUserIds));

                    newEvent.setStatus(com.example.zephyrevents.model.EventStatus.OPEN);

                    String existingUrl = viewModel.existingImgUrl != null ? viewModel.existingImgUrl : "";
                    EventController.getInstance().saveEventWithOptionalImage(
                            newEvent,
                            viewModel.pendingEventImageUri,
                            existingUrl,
                            new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    // If it's an edit AND the deadline was moved to the future, Reset the Waitlist
                                    if (viewModel.isEditMode && newEvent.getRegistrationEndTime() > System.currentTimeMillis()) {
                                        new com.example.zephyrevents.repository.WaitlistRepository().resetWaitlist(newEvent.getEventId(), null);
                                    }
                                    Toast.makeText(requireContext(), "Event Saved Successfully!", Toast.LENGTH_SHORT).show();
                                    requireActivity().finish();

                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Button nextBtn = requireActivity().findViewById(R.id.next_button);
                                    if (nextBtn != null) {
                                        nextBtn.setText("CONFIRM & CREATE");
                                        nextBtn.setEnabled(false);
                                    }

                                    Toast.makeText(requireContext(), "Failed to save event", Toast.LENGTH_SHORT).show();

                                }
                            }
                    );
                }
        );
    }
}