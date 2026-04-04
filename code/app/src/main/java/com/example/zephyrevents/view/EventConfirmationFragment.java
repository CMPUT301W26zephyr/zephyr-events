package com.example.zephyrevents.view;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.controller.SystemLogController;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.model.EventTime;
import com.example.zephyrevents.model.EventViewModel;
import com.example.zephyrevents.model.User;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.repository.UserRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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

        TextView privateBadge = view.findViewById(R.id.confirm_preview_private_badge);
        privateBadge.setVisibility(viewModel.privateEvent ? View.VISIBLE : View.GONE);

        ImageView hero = view.findViewById(R.id.confirm_preview_image);
        if (viewModel.pendingEventImageUri != null) {
            hero.setImageTintList(null);
            hero.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(this).load(viewModel.pendingEventImageUri).centerCrop().into(hero);
        } else if (viewModel.existingImgUrl != null && !viewModel.existingImgUrl.isEmpty()) {
            hero.setImageTintList(null);
            hero.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(this).load(viewModel.existingImgUrl).centerCrop().into(hero);
        } else {
            hero.setImageResource(R.drawable.ic_image_placeholder2);
            hero.setImageTintList(ContextCompat.getColorStateList(requireContext(), android.R.color.darker_gray));
            hero.setScaleType(ImageView.ScaleType.CENTER);
        }

        TextView title = view.findViewById(R.id.confirm_preview_title);
        title.setText(!TextUtils.isEmpty(viewModel.title) ? viewModel.title : getString(R.string.placeholder));

        TextView price = view.findViewById(R.id.confirm_preview_price);
        double priceVal = 0;
        try {
            if (!TextUtils.isEmpty(viewModel.price)) {
                priceVal = Double.parseDouble(viewModel.price.trim());
            }
        } catch (NumberFormatException ignored) {
        }
        price.setText(String.format(Locale.getDefault(), "$%.2f", priceVal));

        TextView eventDate = view.findViewById(R.id.confirm_preview_event_date);
        eventDate.setText(!TextUtils.isEmpty(viewModel.eventDate) ? viewModel.eventDate : getString(R.string.date_tbd));

        String locationStr = viewModel.location != null ? viewModel.location : "";
        if (viewModel.address != null && !viewModel.address.trim().isEmpty()) {
            locationStr = locationStr.isEmpty()
                    ? viewModel.address.trim()
                    : locationStr + " (" + viewModel.address.trim() + ")";
        }
        TextView eventLocation = view.findViewById(R.id.confirm_preview_event_location);
        eventLocation.setText(locationStr.isEmpty() ? getString(R.string.location_tbd) : locationStr);

        TextView about = view.findViewById(R.id.confirm_preview_about);
        if (TextUtils.isEmpty(viewModel.description)) {
            about.setText(R.string.event_preview_no_description);
            about.setTypeface(null, Typeface.ITALIC);
        } else {
            about.setText(viewModel.description.trim());
            about.setTypeface(null, Typeface.NORMAL);
        }

        TextView capacity = view.findViewById(R.id.confirm_preview_capacity);
        capacity.setText(TextUtils.isEmpty(viewModel.attendeeCount) ? "0" : viewModel.attendeeCount);

        TextView waitlistCap = view.findViewById(R.id.confirm_preview_waitlist_cap);
        if (viewModel.waitlistCapacity == null || viewModel.waitlistCapacity.trim().isEmpty()) {
            waitlistCap.setText("Unlimited");
        } else {
            waitlistCap.setText(viewModel.waitlistCapacity.trim());
        }

        TextView applicants = view.findViewById(R.id.confirm_preview_applicants);
        applicants.setText(String.valueOf(viewModel.isEditMode ? viewModel.originalApplicants : 0));

        TextView regEnds = view.findViewById(R.id.confirm_preview_registration_ends);
        regEnds.setText(formatRegistrationEndForPreview(viewModel.registrationPeriod));

        TextView organizerName = view.findViewById(R.id.confirm_preview_organizer_name);
        ImageView organizerAvatar = view.findViewById(R.id.confirm_preview_organizer_avatar);
        bindOrganizerPreview(organizerName, organizerAvatar);

        String finalLocationStr = buildLocationStringForSave(viewModel);
        ((OrganizerEventAddEditView) requireActivity()).setupTopAndBottomUI(
                getString(R.string.event_confirmation_preview_title),
                getString(R.string.confirm_create_button),
                v -> saveEvent(viewModel, finalLocationStr));
    }

    private void bindOrganizerPreview(TextView nameView, ImageView avatarView) {
        UserController uc = new UserController(requireContext());
        String uid = uc.getCurrentUserId();
        if (uid == null || uid.isEmpty()) {
            nameView.setText(R.string.placeholder);
            avatarView.setImageResource(R.drawable.ic_person);
            return;
        }
        new UserRepository().getUserById(uid, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (!isAdded()) return;
                String n = user != null && user.getName() != null && !user.getName().isEmpty()
                        ? user.getName()
                        : getString(R.string.placeholder);
                nameView.setText(n);
                if (user != null && user.getAvatarUrl() != null && !user.getAvatarUrl().trim().isEmpty()) {
                    Glide.with(requireContext())
                            .load(user.getAvatarUrl().trim())
                            .circleCrop()
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .into(avatarView);
                } else {
                    avatarView.setImageResource(R.drawable.ic_person);
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (isAdded()) {
                    nameView.setText(R.string.placeholder);
                    avatarView.setImageResource(R.drawable.ic_person);
                }
            }
        });
    }

    private String formatRegistrationEndForPreview(@Nullable String registrationPeriod) {
        if (TextUtils.isEmpty(registrationPeriod)) {
            return "N/A";
        }
        int idx = registrationPeriod.lastIndexOf(" - ");
        if (idx < 0 || idx + 3 >= registrationPeriod.length()) {
            return "N/A";
        }
        return registrationPeriod.substring(idx + 3).trim();
    }

    private static String buildLocationStringForSave(EventViewModel viewModel) {
        String locationStr = viewModel.location != null ? viewModel.location : "";
        if (viewModel.address != null && !viewModel.address.trim().isEmpty()) {
            locationStr += " (" + viewModel.address.trim() + ")";
        }
        return locationStr;
    }

    private void saveEvent(EventViewModel viewModel, String finalLocationStr) {
        Event newEvent = new Event();

        if (viewModel.isEditMode) {
            newEvent.setEventId(viewModel.eventId);
            newEvent.setOrganizerId(viewModel.organizerId);
            newEvent.setCurrentApplicants(viewModel.originalApplicants);
        } else {
            if (viewModel.eventId == null || viewModel.eventId.isEmpty()) {
                viewModel.eventId = java.util.UUID.randomUUID().toString();
            }
            newEvent.setEventId(viewModel.eventId);
            newEvent.setOrganizerId(new UserController(requireContext()).getCurrentUserId());
        }

        newEvent.setName(viewModel.title);
        newEvent.setDescription(viewModel.description);

        try {
            newEvent.setPrice(Double.parseDouble(
                    TextUtils.isEmpty(viewModel.price) ? "0" : viewModel.price.trim()));
        } catch (Exception e) {
            newEvent.setPrice(0.0);
        }
        try {
            newEvent.setCapacity(Integer.parseInt(
                    TextUtils.isEmpty(viewModel.attendeeCount) ? "0" : viewModel.attendeeCount.trim()));
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
        eventLoc.setRequiresGeolocation(viewModel.requireGeolocation);
        eventLoc.setGeolocationRadiusKm(viewModel.geolocationRadiusKm);

        if (viewModel.eventLat != 0 || viewModel.eventLng != 0) {
            eventLoc.setCoordinate(
                    new com.example.zephyrevents.model.Coordinate(
                            viewModel.eventLat,
                            viewModel.eventLng
                    )
            );
        }

        newEvent.setLocation(eventLoc);

        SimpleDateFormat sdfFull = new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.getDefault());
        SimpleDateFormat sdfDateOnly = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

        try {
            if (viewModel.eventDate != null && !viewModel.eventDate.isEmpty()) {
                Date eDate = null;
                try {
                    eDate = sdfFull.parse(viewModel.eventDate.trim());
                } catch (ParseException e) {
                    eDate = sdfDateOnly.parse(viewModel.eventDate.trim());
                }
                if (eDate != null) {
                    EventTime time = new EventTime(eDate.getTime(), eDate.getTime() + 7200000);
                    newEvent.setTime(time);
                }
            }
            if (viewModel.registrationPeriod != null && viewModel.registrationPeriod.contains(" - ")) {
                String[] parts = viewModel.registrationPeriod.split(" - ");
                Date regStart = null;
                Date regEnd = null;
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

                if (regStart != null) {
                    newEvent.setRegistrationStartTime(regStart.getTime());
                }
                if (regEnd != null) {
                    newEvent.setRegistrationEndTime(regEnd.getTime());
                }
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
                        if (viewModel.isEditMode && newEvent.getRegistrationEndTime() > System.currentTimeMillis()) {
                            new com.example.zephyrevents.repository.WaitlistRepository()
                                    .resetWaitlist(newEvent.getEventId(), null);
                        }
                        Toast.makeText(requireContext(), "Event Saved Successfully!", Toast.LENGTH_SHORT).show();
                        requireActivity().finish();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        android.widget.Button nextBtn = requireActivity().findViewById(R.id.next_button);
                        if (nextBtn != null) {
                            nextBtn.setText(R.string.confirm_create_button);
                            nextBtn.setEnabled(false);
                        }
                        Toast.makeText(requireContext(), "Failed to save event", Toast.LENGTH_SHORT).show();
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

                                    String action = viewModel.isEditMode ? "EVENT_EDITED" : "EVENT_CREATED";
                                    String desc = "Event '" + newEvent.getName() + "' was " + (viewModel.isEditMode ? "edited" : "created");
                                    SystemLogController.getInstance().logAction(action, desc, newEvent.getOrganizerId());

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
