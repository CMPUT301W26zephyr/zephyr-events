package com.example.zephyrevents.view;

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
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventCreateFragment extends Fragment {

    private EventViewModel viewModel;

    // Date Tracking
    private String selectedStartDate = "";
    private String selectedEndDate = "";
    private String selectedEventDate = "";
    private double selectedRadiusMeters = 500;
    private org.osmdroid.events.MapEventsReceiver receiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_create, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        // 1. DEFINE ALL UI ELEMENTS FIRST so they can be used anywhere below
        EditText inputTitle = view.findViewById(R.id.input_event_title);
        AutoCompleteTextView dropdownType = view.findViewById(R.id.eventTypeDropdown);
        EditText inputPrice = view.findViewById(R.id.input_event_price);
        EditText inputDesc = view.findViewById(R.id.input_event_desc);
        EditText inputWaitlist = view.findViewById(R.id.input_waitlist);
        EditText inputAttendeeCount = view.findViewById(R.id.input_attendee_count);

        EditText inputLocation = view.findViewById(R.id.input_location);
        EditText inputAddress = view.findViewById(R.id.input_address);
        Button btnFindAddress = view.findViewById(R.id.btn_find_address);
        AutoCompleteTextView dropdownRadius = view.findViewById(R.id.radiusDropdown);

        // The following map setup, radius dropdown, and tap receiver is from Anthropic, Claude (claude.ai), "Android osmdroid map with radius dropdown selector and tap-to-place circle overlay", 2025-03-30
        String[] radiusOptions = new String[]{"100 m", "250 m", "500 m", "1000 m"};
        dropdownRadius.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                radiusOptions
        ));

        dropdownRadius.setText("500 m", false);

        dropdownRadius.setOnItemClickListener((parent, view1, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);

            switch (selected) {
                case "100 m":
                    selectedRadiusMeters = 100;
                    viewModel.geolocationRadiusKm = selectedRadiusMeters / 1000.0;
                    break;
                case "250 m":
                    selectedRadiusMeters = 250;
                    viewModel.geolocationRadiusKm = selectedRadiusMeters / 1000.0;
                    break;
                case "500 m":
                    selectedRadiusMeters = 500;
                    viewModel.geolocationRadiusKm = selectedRadiusMeters / 1000.0;
                    break;
                case "1000 m":
                    selectedRadiusMeters = 1000;
                    viewModel.geolocationRadiusKm = selectedRadiusMeters / 1000.0;
                    break;
            }
        });
        SwitchMaterial switchGeo = view.findViewById(R.id.switch_geolocation);


        org.osmdroid.views.MapView mapView = view.findViewById(R.id.map_view);
        mapView.setVisibility(switchGeo.isChecked() ? View.VISIBLE : View.GONE);

        switchGeo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mapView.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        org.osmdroid.config.Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(14.0);
        mapView.getController().setCenter(new org.osmdroid.util.GeoPoint(53.5461, -113.4938));

        btnFindAddress.setOnClickListener(v -> {
            moveMapToTypedAddress(inputAddress.getText().toString().trim(), mapView);
        });
            receiver = new org.osmdroid.events.MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(org.osmdroid.util.GeoPoint p) {
                mapView.getOverlays().clear();
                mapView.getOverlays().add(new org.osmdroid.views.overlay.MapEventsOverlay(receiver));

                org.osmdroid.views.overlay.Polygon circle = new org.osmdroid.views.overlay.Polygon();
                circle.setPoints(org.osmdroid.views.overlay.Polygon.pointsAsCircle(p, selectedRadiusMeters));
                circle.setFillColor(0x33E53935);
                circle.setStrokeColor(0x99E53935);
                circle.setStrokeWidth(2f);
                mapView.getOverlays().add(circle);

                viewModel.eventLat = p.getLatitude();
                viewModel.eventLng = p.getLongitude();

                mapView.invalidate();
                return true;
            }

            @Override
            public boolean longPressHelper(org.osmdroid.util.GeoPoint p) { return false; }
        };

        mapView.getOverlays().add(new org.osmdroid.views.overlay.MapEventsOverlay(receiver));

// Restore pin if returning from confirmation
        if (viewModel.eventLat != 0 && viewModel.eventLng != 0) {
            mapView.getController().setCenter(new org.osmdroid.util.GeoPoint(viewModel.eventLat, viewModel.eventLng));
        }

        HorizontalScrollView scrollRegistration = view.findViewById(R.id.scroll_registration);
        View columnRegEnd = view.findViewById(R.id.column_reg_end);
        CalendarView calStart = view.findViewById(R.id.calendar_reg_start);
        CalendarView calEnd = view.findViewById(R.id.calendar_reg_end);
        CalendarView calEvent = view.findViewById(R.id.calendar_event);
        TextView textRegPeriod = view.findViewById(R.id.text_reg_period);
        TextView textEvent = view.findViewById(R.id.text_event_date);

        Button btnDelete = view.findViewById(R.id.btn_delete_event);

        // Setup Dropdown
        String[] eventTypes = new String[]{"Educational", "Workshop", "Corporate", "Social", "Recreation", "Entertainment", "Networking", "Other"};
        dropdownType.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, eventTypes));

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

            // Fetch the existing event from Firebase if we haven't loaded it yet
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

                            // Separate the address from the location string if parenthesis exist
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

                            viewModel.isDataLoaded = true;

                            // Push downloaded data directly to UI
                            inputTitle.setText(viewModel.title);
                            inputPrice.setText(viewModel.price);
                            inputDesc.setText(viewModel.description);
                            inputAttendeeCount.setText(viewModel.attendeeCount);
                            inputLocation.setText(viewModel.location);
                            inputAddress.setText(viewModel.address);
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

        // 3. RESTORE EXISTING VIEWMODEL DATA (If returning from confirmation screen)
        inputTitle.setText(viewModel.title);
        dropdownType.setText(viewModel.type, false);
        inputPrice.setText(viewModel.price);
        inputDesc.setText(viewModel.description);
        inputWaitlist.setText(viewModel.waitlistCapacity);
        inputAttendeeCount.setText(viewModel.attendeeCount);
        inputLocation.setText(viewModel.location);
        inputAddress.setText(viewModel.address);
        switchGeo.setChecked(viewModel.requireGeolocation);

        // 4. SETUP CALENDARS
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        long today = System.currentTimeMillis() - 1000;
        calStart.setMinDate(today);
        calEnd.setMinDate(today);
        calEvent.setMinDate(today);

        restoreCalendarState(calStart, calEnd, calEvent, textRegPeriod, textEvent, sdf);

        calStart.setOnDateChangeListener((calView, year, month, dayOfMonth) -> {
            Calendar c = Calendar.getInstance(); c.set(year, month, dayOfMonth);
            long selectedMillis = c.getTimeInMillis();
            selectedStartDate = sdf.format(c.getTime());
            updateRegistrationText(textRegPeriod);

            calEnd.setMinDate(selectedMillis);
            if (calEnd.getDate() < selectedMillis) {
                calEnd.setDate(selectedMillis, true, true);
                selectedEndDate = "";
                updateRegistrationText(textRegPeriod);
            }

            if (calEvent.getMinDate() < selectedMillis) {
                calEvent.setMinDate(selectedMillis);
                if (calEvent.getDate() < selectedMillis) {
                    calEvent.setDate(selectedMillis, true, true);
                    selectedEventDate = "";
                    textEvent.setText("Select an event date above");
                    textEvent.setTextColor(Color.parseColor("#F44336"));
                }
            }
            scrollRegistration.post(() -> scrollRegistration.smoothScrollTo(columnRegEnd.getLeft(), 0));
        });

        calEnd.setOnDateChangeListener((calView, year, month, dayOfMonth) -> {
            Calendar c = Calendar.getInstance(); c.set(year, month, dayOfMonth);
            long selectedMillis = c.getTimeInMillis();
            selectedEndDate = sdf.format(c.getTime());
            updateRegistrationText(textRegPeriod);

            calEvent.setMinDate(selectedMillis);
            if (calEvent.getDate() < selectedMillis) {
                calEvent.setDate(selectedMillis, true, true);
                selectedEventDate = "";
                textEvent.setText("Select an event date above");
                textEvent.setTextColor(Color.parseColor("#F44336"));
            }
        });

        calEvent.setOnDateChangeListener((calView, year, month, dayOfMonth) -> {
            Calendar c = Calendar.getInstance(); c.set(year, month, dayOfMonth);
            selectedEventDate = sdf.format(c.getTime());
            textEvent.setText("Official event date: " + selectedEventDate);
            textEvent.setTextColor(Color.parseColor("#888888"));
        });



        // 5. VALIDATION AND NAVIGATION
        String topBarTitle = viewModel.isEditMode ? "Edit Event" : "Create Event";

        ((OrganizerEventAddEditView) requireActivity()).setupTopAndBottomUI(
                topBarTitle, "NEXT", v -> {
                    boolean isValid = true;

                    if (inputTitle.getText().toString().trim().isEmpty()) { inputTitle.setError("Required"); isValid = false; }
                    if (inputAttendeeCount.getText().toString().trim().isEmpty()) { inputAttendeeCount.setError("Required"); isValid = false; }
                    if (inputLocation.getText().toString().trim().isEmpty()) { inputLocation.setError("Required"); isValid = false; }
                    if (switchGeo.isChecked() && (viewModel.eventLat == 0 || viewModel.eventLng == 0)) {
                        Toast.makeText(requireContext(), "Please select a location on the map.", Toast.LENGTH_SHORT).show();
                        isValid = false;
                    }
                    if (selectedStartDate.isEmpty() || selectedEndDate.isEmpty()) {
                        Toast.makeText(requireContext(), "Please select Registration Start and End dates.", Toast.LENGTH_SHORT).show();
                        isValid = false;
                    } else if (selectedEventDate.isEmpty()) {
                        Toast.makeText(requireContext(), "Please select an Official Event Date.", Toast.LENGTH_SHORT).show();
                        isValid = false;
                    }

                    if (isValid) {
                        viewModel.title = inputTitle.getText().toString().trim();
                        viewModel.type = dropdownType.getText().toString().trim();
                        viewModel.price = inputPrice.getText().toString().trim();
                        viewModel.description = inputDesc.getText().toString().trim();
                        viewModel.waitlistCapacity = inputWaitlist.getText().toString().trim();
                        viewModel.attendeeCount = inputAttendeeCount.getText().toString().trim();
                        viewModel.location = inputLocation.getText().toString().trim();
                        viewModel.address = inputAddress.getText().toString().trim();
                        viewModel.requireGeolocation = switchGeo.isChecked();
                        viewModel.registrationPeriod = selectedStartDate + " - " + selectedEndDate;
                        viewModel.eventDate = selectedEventDate;
                        // TODO: Capture the event venue's lat/lng here after the organizer enters
                        // the address or selects a map pin, then carry those coordinates forward
                        // so EventConfirmationFragment can store them in Event.location.coordinate.

                        ((OrganizerEventAddEditView) requireActivity()).navigateToFragment(new EventConfirmationFragment(), true);
                    }
                }
        );
    }

    // The following function is from Anthropic, Claude (claude.ai), "Android map function to geocode a typed address and display a radius circle using osmdroid", 2025-03-30
    private void moveMapToTypedAddress(String addressText, org.osmdroid.views.MapView mapView) {
        if (addressText == null || addressText.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Please enter an address.", Toast.LENGTH_SHORT).show();
            return;
        }

        android.location.Geocoder geocoder = new android.location.Geocoder(requireContext(), java.util.Locale.getDefault());

        try {
            java.util.List<android.location.Address> results = geocoder.getFromLocationName(addressText, 1);

            if (results == null || results.isEmpty()) {
                Toast.makeText(requireContext(), "Address not found.", Toast.LENGTH_SHORT).show();
                return;
            }

            android.location.Address result = results.get(0);
            double lat = result.getLatitude();
            double lng = result.getLongitude();

            org.osmdroid.util.GeoPoint point = new org.osmdroid.util.GeoPoint(lat, lng);

            mapView.getOverlays().clear();
            mapView.getOverlays().add(new org.osmdroid.views.overlay.MapEventsOverlay(receiver));


            org.osmdroid.views.overlay.Polygon circle = new org.osmdroid.views.overlay.Polygon();
            circle.setPoints(org.osmdroid.views.overlay.Polygon.pointsAsCircle(point, selectedRadiusMeters));
            circle.setFillColor(0x33E53935);
            circle.setStrokeColor(0x99E53935);
            circle.setStrokeWidth(2f);
            mapView.getOverlays().add(circle);

            viewModel.eventLat = lat;
            viewModel.eventLng = lng;

            mapView.getController().setZoom(16.0);
            mapView.getController().setCenter(point);
            mapView.invalidate();

            Toast.makeText(requireContext(), "Address found.", Toast.LENGTH_SHORT).show();

        } catch (java.io.IOException e) {
            Toast.makeText(requireContext(), "Could not search address.", Toast.LENGTH_SHORT).show();
        }
    }
    private void updateRegistrationText(TextView textView) {
        String startText = selectedStartDate.isEmpty() ? "..." : selectedStartDate;
        String endText = selectedEndDate.isEmpty() ? "..." : selectedEndDate;
        textView.setText("Registration: " + startText + " to " + endText);
        if (!selectedStartDate.isEmpty() && !selectedEndDate.isEmpty()) {
            textView.setTextColor(Color.parseColor("#888888"));
        } else {
            textView.setTextColor(Color.parseColor("#F44336"));
        }
    }

    private void restoreCalendarState(CalendarView calStart, CalendarView calEnd, CalendarView calEvent, TextView textRegPeriod, TextView textEvent, SimpleDateFormat sdf) {
        try {
            if (viewModel.registrationPeriod != null && viewModel.registrationPeriod.contains(" - ")) {
                String[] parts = viewModel.registrationPeriod.split(" - ");
                selectedStartDate = parts[0]; selectedEndDate = parts[1];
                Date startDate = sdf.parse(selectedStartDate); Date endDate = sdf.parse(selectedEndDate);

                if (startDate != null) { calStart.setDate(startDate.getTime(), false, true); calEnd.setMinDate(startDate.getTime()); }
                if (endDate != null) { calEnd.setDate(endDate.getTime(), false, true); calEvent.setMinDate(endDate.getTime()); }
                updateRegistrationText(textRegPeriod);
            }
            if (viewModel.eventDate != null && !viewModel.eventDate.isEmpty()) {
                selectedEventDate = viewModel.eventDate;
                Date eventDate = sdf.parse(selectedEventDate);
                if (eventDate != null) { calEvent.setDate(eventDate.getTime(), false, true); }
                textEvent.setText("Official event date: " + selectedEventDate);
                textEvent.setTextColor(Color.parseColor("#888888"));
            }
        } catch (ParseException e) { e.printStackTrace(); }
    }
}
