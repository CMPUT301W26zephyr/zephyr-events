package com.example.zephyrevents.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.model.EventViewModel;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.net.Uri;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import com.bumptech.glide.Glide;

import androidx.appcompat.app.AlertDialog;


public class EventCreateFragment extends Fragment {

    private EventViewModel viewModel;

    // Use Calendars to precisely track exact milliseconds for validation
    private final Calendar regStartCal = Calendar.getInstance();
    private final Calendar regEndCal = Calendar.getInstance();
    private final Calendar eventCal = Calendar.getInstance();

    // Formatters to include both date and time for the UI and ViewModel
    private final SimpleDateFormat sdfFull = new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.getDefault());
    private final SimpleDateFormat sdfDateOnly = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    private Button nextButton;

    // Track if user has interacted with the calendars so we don't prematurely validate empty forms
    private boolean startSelected = false;
    private boolean endSelected = false;
    private boolean eventSelected = false;

    // Grab references for auto-scrolling and validation
    private HorizontalScrollView scrollRegistration;
    private View columnRegEnd;
    private View columnEvent;
    private CalendarView calStart, calEnd, calEvent;
    private TimePicker timeStart, timeEnd, timeEvent;

    // Class-level variables so validateDates() can access them
    private TextView textRegPeriod;
    private TextView textEvent;

    private ActivityResultLauncher<PickVisualMediaRequest> pickEventImg;
    private ImageView eventImgPreview;

    private boolean eventHasPoster() {
        return viewModel != null
                && (viewModel.pendingEventImageUri != null
                || (viewModel.existingImgUrl != null && !viewModel.existingImgUrl.isEmpty()));


    }

    private void launchPickEventPoster() {
        pickEventImg.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void clearEventPoster() {
        viewModel.pendingEventImageUri = null;
        viewModel.existingImgUrl = "";
        if (eventImgPreview != null) {
            Glide.with(this).clear(eventImgPreview);
            eventImgPreview.setImageResource(R.drawable.ic_image_placeholder2);
        }
    }

    private void showConfirmRemovePoster() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_remove_poster, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();
        dialogView.findViewById(R.id.btnDialogCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnDialogConfirm).setOnClickListener(v -> {
            dialog.dismiss();
            clearEventPoster();
            Toast.makeText(requireContext(), "Poster removed", Toast.LENGTH_SHORT).show();

        });
        dialog.show();
    }

    private void showPosterOptionDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_event_poster, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();
        dialogView.findViewById(R.id.btnDialogCancel).setOnClickListener(v -> {
            dialog.dismiss();
            launchPickEventPoster();
        });

        dialogView.findViewById(R.id.btnDialogConfirm).setOnClickListener(v -> {
            dialog.dismiss();
            showConfirmRemovePoster();

        });
        dialog.show();

    }


    @Override
    public void onCreate(@Nullable Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        pickEventImg = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri == null) return;
                    EventViewModel vm = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
                    vm.pendingEventImageUri = uri;
                    vm.existingImgUrl = "";
                    if (eventImgPreview != null) {
                        Glide.with(this).load(uri).centerCrop().into(eventImgPreview);
                    }
                }
        );


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_create, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        // 1. DEFINE ALL UI ELEMENTS
        EditText inputTitle = view.findViewById(R.id.input_event_title);
        EditText inputPrice = view.findViewById(R.id.input_event_price);
        EditText inputDesc = view.findViewById(R.id.input_event_desc);
        EditText inputWaitlist = view.findViewById(R.id.input_waitlist);
        EditText inputAttendeeCount = view.findViewById(R.id.input_attendee_count);

        EditText inputLocation = view.findViewById(R.id.input_location);
        EditText inputAddress = view.findViewById(R.id.input_address);
        SwitchMaterial switchGeo = view.findViewById(R.id.switch_geolocation);

        textRegPeriod = view.findViewById(R.id.text_reg_period);
        textEvent = view.findViewById(R.id.text_event_date);

        scrollRegistration = view.findViewById(R.id.scroll_registration);
        columnRegEnd = view.findViewById(R.id.column_reg_end);
        columnEvent = view.findViewById(R.id.column_event_date);

        calStart = view.findViewById(R.id.calendar_reg_start);
        timeStart = view.findViewById(R.id.time_reg_start);

        calEnd = view.findViewById(R.id.calendar_reg_end);
        timeEnd = view.findViewById(R.id.time_reg_end);

        calEvent = view.findViewById(R.id.calendar_event);
        timeEvent = view.findViewById(R.id.time_event);

        nextButton = requireActivity().findViewById(R.id.next_button);

        Button btnDelete = view.findViewById(R.id.btn_delete_event);
        MaterialButtonToggleGroup toggleVisibility = view.findViewById(R.id.toggle_event_visibility);
        View privateWarning = view.findViewById(R.id.private_event_warning);
        Button btnInviteEntrants = view.findViewById(R.id.btn_invite_entrants_private);
        Button btnInviteCoorg = view.findViewById(R.id.btn_invite_coorganizer);
        View dividerInvites = view.findViewById(R.id.divider_event_invites);
        View rowInviteButtons = view.findViewById(R.id.row_invite_buttons);
        TextView labelInvites = view.findViewById(R.id.label_event_form_invites);

        toggleVisibility.check(viewModel.privateEvent ? R.id.btn_visibility_private : R.id.btn_visibility_public);
        privateWarning.setVisibility(viewModel.privateEvent ? View.VISIBLE : View.GONE);
        toggleVisibility.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            viewModel.privateEvent = checkedId == R.id.btn_visibility_private;
            privateWarning.setVisibility(viewModel.privateEvent ? View.VISIBLE : View.GONE);
            updateInviteButtons(dividerInvites, labelInvites, rowInviteButtons, btnInviteEntrants, btnInviteCoorg);
        });

        Runnable setupInvites = () -> updateInviteButtons(dividerInvites, labelInvites, rowInviteButtons, btnInviteEntrants, btnInviteCoorg);
        btnInviteEntrants.setOnClickListener(v -> {
            if (viewModel.eventId == null) return;
            Intent i = new Intent(requireContext(), InviteUsersActivity.class);
            i.putExtra(InviteUsersActivity.EXTRA_EVENT_ID, viewModel.eventId);
            i.putExtra(InviteUsersActivity.EXTRA_MODE, InviteUsersActivity.MODE_PRIVATE_WAITLIST);
            startActivity(i);
        });
        btnInviteCoorg.setOnClickListener(v -> {
            if (viewModel.eventId == null) return;
            Intent i = new Intent(requireContext(), InviteUsersActivity.class);
            i.putExtra(InviteUsersActivity.EXTRA_EVENT_ID, viewModel.eventId);
            i.putExtra(InviteUsersActivity.EXTRA_MODE, InviteUsersActivity.MODE_CO_ORG);
            startActivity(i);
        });
        setupInvites.run();

        eventImgPreview = view.findViewById(R.id.event_image_preview);
        View eventImgContainer = view.findViewById(R.id.event_image_container);

        eventImgContainer.setOnClickListener(v -> {
            if (eventHasPoster()) {
                showPosterOptionDialog();
            } else {
                launchPickEventPoster();
            }
        });

        if (viewModel.pendingEventImageUri != null) {
            Glide.with(this).load(viewModel.pendingEventImageUri).centerCrop().into(eventImgPreview);

        } else if (viewModel.existingImgUrl != null && !viewModel.existingImgUrl.isEmpty()) {
            Glide.with(this).load(viewModel.existingImgUrl).centerCrop().into(eventImgPreview);
        }

        // 2. CHECK EDIT MODE AND FETCH DATA
        if (viewModel.isEditMode) {
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> {
                EventController.getInstance().deleteEvent(viewModel.eventId, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(requireContext(), "Event Deleted", Toast.LENGTH_SHORT).show();
                        requireActivity().finish();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(requireContext(), "Failed to delete event", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            if (!viewModel.isDataLoaded) {
                EventController.getInstance().getEventById(viewModel.eventId, new RepositoryCallback<Event>() {
                    @Override
                    public void onSuccess(Event e) {
                        if (e != null) {
                            viewModel.title = e.getName() != null ? e.getName() : "";
                            viewModel.price = String.valueOf(e.getPrice());
                            viewModel.description = e.getDescription() != null ? e.getDescription() : "";
                            viewModel.attendeeCount = String.valueOf(e.getCapacity());
                            viewModel.organizerId = e.getOrganizerId();
                            viewModel.originalApplicants = e.getCurrentApplicants();

                            if (e.getLocation() != null && e.getLocation().getLocationString() != null) {
                                String fullLoc = e.getLocation().getLocationString();
                                if (fullLoc.contains("(") && fullLoc.contains(")")) {
                                    int openP = fullLoc.indexOf("(");
                                    int closeP = fullLoc.lastIndexOf(")");
                                    viewModel.location = fullLoc.substring(0, openP).trim();
                                    viewModel.address = fullLoc.substring(openP + 1, closeP).trim();
                                } else {
                                    viewModel.location = fullLoc;
                                }
                            }

                            long regStart = e.getRegistrationStartTime();
                            long regEnd = e.getRegistrationEndTime();
                            long eventTime = e.getTime() != null ? e.getTime().getStartTime() : 0;

                            if (regEnd > 0) {
                                long actualStart = regStart > 0 ? regStart : System.currentTimeMillis();
                                viewModel.registrationPeriod = sdfFull.format(new Date(actualStart)) + " - " + sdfFull.format(new Date(regEnd));
                            }

                            if (eventTime > 0) {
                                viewModel.eventDate = sdfFull.format(new Date(eventTime));
                            }

                            restoreCalendarState(calStart, timeStart, calEnd, timeEnd, calEvent, timeEvent);

                            viewModel.isDataLoaded = true;
                            viewModel.privateEvent = e.isPrivateEvent();
                            viewModel.coOrganizerUserIds = new ArrayList<>(e.getCoOrganizerUserIds());
                            viewModel.pendingPrivateWaitlistInviteUserIds = new ArrayList<>(e.getPendingPrivateWaitlistInviteUserIds());

                            inputTitle.setText(viewModel.title);
                            inputPrice.setText(viewModel.price);
                            inputDesc.setText(viewModel.description);
                            inputAttendeeCount.setText(viewModel.attendeeCount);
                            inputLocation.setText(viewModel.location);
                            inputAddress.setText(viewModel.address);

                            toggleVisibility.check(viewModel.privateEvent ? R.id.btn_visibility_private : R.id.btn_visibility_public);
                            privateWarning.setVisibility(viewModel.privateEvent ? View.VISIBLE : View.GONE);
                            updateInviteButtons(dividerInvites, labelInvites, rowInviteButtons, btnInviteEntrants, btnInviteCoorg);

                            if (e.getImageUrl() != null && !e.getImageUrl().isEmpty()) {
                                viewModel.existingImgUrl = e.getImageUrl();
                                viewModel.pendingEventImageUri = null;
                                Glide.with(EventCreateFragment.this).load(e.getImageUrl()).centerCrop().into(eventImgPreview);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(requireContext(), "Failed to load event data for editing", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            btnDelete.setVisibility(View.GONE);
        }

        // 3. RESTORE EXISTING VIEWMODEL DATA
        inputTitle.setText(viewModel.title);
        inputPrice.setText(viewModel.price);
        inputDesc.setText(viewModel.description);
        inputWaitlist.setText(viewModel.waitlistCapacity);
        inputAttendeeCount.setText(viewModel.attendeeCount);
        inputLocation.setText(viewModel.location);
        inputAddress.setText(viewModel.address);
        switchGeo.setChecked(viewModel.requireGeolocation);

        // 4. SETUP CALENDARS AND TIME PICKERS
        long today = System.currentTimeMillis() - 1000;
        if (!viewModel.isEditMode) {
            calStart.setMinDate(today);
            calEnd.setMinDate(today);
            calEvent.setMinDate(today);
        }

        setupDateAndTime(calStart, timeStart, regStartCal, 1);
        setupDateAndTime(calEnd, timeEnd, regEndCal, 2);
        setupDateAndTime(calEvent, timeEvent, eventCal, 3);

        restoreCalendarState(calStart, timeStart, calEnd, timeEnd, calEvent, timeEvent);

        // 5. VALIDATION AND NAVIGATION
        String topBarTitle = viewModel.isEditMode ? "Edit Event" : "Create Event";

        ((OrganizerEventAddEditView) requireActivity()).setupTopAndBottomUI(
                topBarTitle, "NEXT", v -> {
                    boolean isValid = true;
                    StringBuilder errorMsg = new StringBuilder("Please fill in the required fields\n");

                    if (inputTitle.getText().toString().trim().isEmpty() || inputAttendeeCount.getText().toString().trim().isEmpty() ||
                            inputLocation.getText().toString().trim().isEmpty() || !startSelected || !endSelected || regStartCal.getTimeInMillis()
                            >= regEndCal.getTimeInMillis() || !eventSelected || (endSelected && regEndCal.getTimeInMillis() >= eventCal.getTimeInMillis())) {
                        isValid = false;
                    }

                    if (isValid) {
                        viewModel.title = inputTitle.getText().toString().trim();
                        viewModel.price = inputPrice.getText().toString().trim();
                        viewModel.description = inputDesc.getText().toString().trim();
                        viewModel.waitlistCapacity = inputWaitlist.getText().toString().trim();
                        viewModel.attendeeCount = inputAttendeeCount.getText().toString().trim();
                        viewModel.location = inputLocation.getText().toString().trim();
                        viewModel.address = inputAddress.getText().toString().trim();
                        viewModel.requireGeolocation = switchGeo.isChecked();

                        // Save the full formatted strings (Date + Time) to the ViewModel
                        viewModel.registrationPeriod = sdfFull.format(regStartCal.getTime()) + " - " + sdfFull.format(regEndCal.getTime());
                        viewModel.eventDate = sdfFull.format(eventCal.getTime());

                        ((OrganizerEventAddEditView) requireActivity()).navigateToFragment(new EventConfirmationFragment(), true);
                    } else {
                        Toast.makeText(requireContext(), errorMsg.toString().trim(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel.isEditMode && viewModel.eventId != null) {
            EventController.getInstance().getEventById(viewModel.eventId, new RepositoryCallback<Event>() {
                @Override
                public void onSuccess(Event e) {
                    if (e == null || getView() == null) return;
                    viewModel.privateEvent = e.isPrivateEvent();
                    viewModel.coOrganizerUserIds = new ArrayList<>(e.getCoOrganizerUserIds());
                    viewModel.pendingPrivateWaitlistInviteUserIds = new ArrayList<>(e.getPendingPrivateWaitlistInviteUserIds());
                }

                @Override
                public void onFailure(Exception e) {
                }
            });
        }
    }

    private void updateInviteButtons(View dividerInvites, TextView labelInvites, View rowInviteButtons,
                                     Button btnInviteEntrants, Button btnInviteCoorg) {
        boolean edit = viewModel.isEditMode && viewModel.eventId != null;
        boolean showCoorg = edit;
        boolean showEntrants = edit && viewModel.privateEvent;
        btnInviteCoorg.setVisibility(showCoorg ? View.VISIBLE : View.GONE);
        btnInviteEntrants.setVisibility(showEntrants ? View.VISIBLE : View.GONE);
        boolean showSection = showCoorg || showEntrants;
        int sectionVis = showSection ? View.VISIBLE : View.GONE;
        if (dividerInvites != null) dividerInvites.setVisibility(sectionVis);
        if (labelInvites != null) labelInvites.setVisibility(sectionVis);
        if (rowInviteButtons != null) rowInviteButtons.setVisibility(sectionVis);
    }

    private void setupDateAndTime(CalendarView calView, TimePicker timePicker, Calendar tracker, int type) {
        Calendar now = Calendar.getInstance();
        timePicker.setHour(now.get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(now.get(Calendar.MINUTE));

        tracker.setTimeInMillis(calView.getDate());
        tracker.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
        tracker.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
        tracker.set(Calendar.SECOND, 0);

        calView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            tracker.set(Calendar.YEAR, year);
            tracker.set(Calendar.MONTH, month);
            tracker.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            long selectedMillis = tracker.getTimeInMillis();

            if (type == 1) { // Reg Start
                startSelected = true;
                if (calEnd.getDate() < selectedMillis) {
                    calEnd.setDate(selectedMillis, false, true);
                    regEndCal.setTimeInMillis(selectedMillis);
                }
                calEnd.setMinDate(selectedMillis);
                scrollRegistration.postDelayed(() -> scrollRegistration.smoothScrollTo(columnRegEnd.getLeft(), 0), 300);

            } else if (type == 2) { // Reg End
                endSelected = true;
                if (calEvent.getDate() < selectedMillis) {
                    calEvent.setDate(selectedMillis, false, true);
                    eventCal.setTimeInMillis(selectedMillis);
                }
                calEvent.setMinDate(selectedMillis);
                scrollRegistration.postDelayed(() -> scrollRegistration.smoothScrollTo(columnEvent.getLeft(), 0), 300);

            } else if (type == 3) { // Event Date
                eventSelected = true;
            }

            validateDates();
        });

        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            tracker.set(Calendar.HOUR_OF_DAY, hourOfDay);
            tracker.set(Calendar.MINUTE, minute);

            if (type == 1) startSelected = true;
            else if (type == 2) endSelected = true;
            else if (type == 3) eventSelected = true;

            validateDates();
        });
    }

    private void validateDates() {
        if (textRegPeriod == null || textEvent == null) return;

        if (startSelected || endSelected) {
            String startText = startSelected ? sdfFull.format(regStartCal.getTime()) : "...";
            String endText = endSelected ? sdfFull.format(regEndCal.getTime()) : "...";

            if (startSelected && endSelected) {
                if (regStartCal.getTimeInMillis() < regEndCal.getTimeInMillis()) {
                    textRegPeriod.setText("Registration: " + startText + " to " + endText);
                    textRegPeriod.setTextColor(Color.parseColor("#888888"));
                } else {
                    textRegPeriod.setText("Registration start must be before end.");
                    textRegPeriod.setTextColor(Color.parseColor("#F44336"));
                }
            } else {
                textRegPeriod.setText("Registration: " + startText + " to " + endText);
                textRegPeriod.setTextColor(Color.parseColor("#F44336"));
            }
        }

        if (eventSelected) {
            if (endSelected) {
                if (regEndCal.getTimeInMillis() < eventCal.getTimeInMillis()) {
                    textEvent.setText("Official Event Date: " + sdfFull.format(eventCal.getTime()));
                    textEvent.setTextColor(Color.parseColor("#888888"));
                } else {
                    textEvent.setText("Event date must be after registration ends.");
                    textEvent.setTextColor(Color.parseColor("#F44336"));
                }
            } else {
                textEvent.setText("Official Event Date: " + sdfFull.format(eventCal.getTime()));
                textEvent.setTextColor(Color.parseColor("#888888"));
            }
        }
    }

    private void restoreCalendarState(CalendarView calStart, TimePicker timeStart,
                                      CalendarView calEnd, TimePicker timeEnd,
                                      CalendarView calEvent, TimePicker timeEvent) {
        try {
            if (viewModel.registrationPeriod != null && viewModel.registrationPeriod.contains(" - ")) {
                String[] parts = viewModel.registrationPeriod.split(" - ");

                Date startDate = parseDateLenient(parts[0].trim());
                Date endDate = parseDateLenient(parts[1].trim());

                if (startDate != null) {
                    regStartCal.setTime(startDate);
                    calStart.setDate(startDate.getTime(), false, true);
                    timeStart.setHour(regStartCal.get(Calendar.HOUR_OF_DAY));
                    timeStart.setMinute(regStartCal.get(Calendar.MINUTE));
                    startSelected = true;
                }
                if (endDate != null) {
                    regEndCal.setTime(endDate);
                    calEnd.setDate(endDate.getTime(), false, true);
                    timeEnd.setHour(regEndCal.get(Calendar.HOUR_OF_DAY));
                    timeEnd.setMinute(regEndCal.get(Calendar.MINUTE));
                    endSelected = true;
                }
            }
            if (viewModel.eventDate != null && !viewModel.eventDate.isEmpty()) {
                Date eventD = parseDateLenient(viewModel.eventDate.trim());
                if (eventD != null) {
                    eventCal.setTime(eventD);
                    calEvent.setDate(eventD.getTime(), false, true);
                    timeEvent.setHour(eventCal.get(Calendar.HOUR_OF_DAY));
                    timeEvent.setMinute(eventCal.get(Calendar.MINUTE));
                    eventSelected = true;
                }
            }
            validateDates();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Date parseDateLenient(String dateStr) {
        try {
            return sdfFull.parse(dateStr.trim());
        } catch (ParseException e) {
            try {
                return sdfDateOnly.parse(dateStr.trim());
            } catch (ParseException ex) {
                return null;
            }
        }
    }
}