package com.example.zephyrevents.view;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.zephyrevents.R;
import com.example.zephyrevents.model.EventViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventDatesFragment extends Fragment {

    private EventViewModel viewModel;

    private String selectedStartDate = "";
    private String selectedEndDate = "";
    private String selectedEventDate = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_dates, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        HorizontalScrollView scrollRegistration = view.findViewById(R.id.scroll_registration);
        View columnRegEnd = view.findViewById(R.id.column_reg_end);

        CalendarView calStart = view.findViewById(R.id.calendar_reg_start);
        CalendarView calEnd = view.findViewById(R.id.calendar_reg_end);
        CalendarView calEvent = view.findViewById(R.id.calendar_event);

        TextView textRegPeriod = view.findViewById(R.id.text_reg_period);
        TextView textEvent = view.findViewById(R.id.text_event_date);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

        long today = System.currentTimeMillis() - 1000;
        calStart.setMinDate(today);
        calEnd.setMinDate(today);
        calEvent.setMinDate(today);

        restoreCalendarState(calStart, calEnd, calEvent, textRegPeriod, textEvent, sdf);

        calStart.setOnDateChangeListener((calView, year, month, dayOfMonth) -> {
            Calendar c = Calendar.getInstance();
            c.set(year, month, dayOfMonth);
            long selectedMillis = c.getTimeInMillis();
            selectedStartDate = sdf.format(c.getTime());

            updateRegistrationText(textRegPeriod);

            calEnd.setMinDate(selectedMillis);
            if (calEnd.getDate() < selectedMillis) {
                calEnd.setDate(selectedMillis, true, true);
                selectedEndDate = ""; // Clear end date so they are forced to re-pick it
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
            Calendar c = Calendar.getInstance();
            c.set(year, month, dayOfMonth);
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
            Calendar c = Calendar.getInstance();
            c.set(year, month, dayOfMonth);
            selectedEventDate = sdf.format(c.getTime());

            textEvent.setText("Official event date: " + selectedEventDate);
            textEvent.setTextColor(Color.parseColor("#888888"));
        });

        ((OrganizerEventAddEditView) requireActivity()).setupTopAndBottomUI(
                "Create Event", "NEXT", v -> {
                    if (selectedStartDate.isEmpty() || selectedEndDate.isEmpty()) {
                        Toast.makeText(requireContext(), "Please select both Registration Start and End dates.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (selectedEventDate.isEmpty()) {
                        Toast.makeText(requireContext(), "Please select an Official Event Date.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    viewModel.registrationPeriod = selectedStartDate + " - " + selectedEndDate;
                    viewModel.eventDate = selectedEventDate;

                    ((OrganizerEventAddEditView) requireActivity()).navigateToFragment(new EventConfirmationFragment(), true);
                }
        );
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

    private void restoreCalendarState(CalendarView calStart, CalendarView calEnd, CalendarView calEvent,
                                      TextView textRegPeriod, TextView textEvent, SimpleDateFormat sdf) {
        try {
            if (viewModel.registrationPeriod != null && viewModel.registrationPeriod.contains(" - ")) {
                String[] parts = viewModel.registrationPeriod.split(" - ");
                selectedStartDate = parts[0];
                selectedEndDate = parts[1];

                Date startDate = sdf.parse(selectedStartDate);
                Date endDate = sdf.parse(selectedEndDate);

                if (startDate != null) {
                    calStart.setDate(startDate.getTime(), false, true);
                    calEnd.setMinDate(startDate.getTime());
                }
                if (endDate != null) {
                    calEnd.setDate(endDate.getTime(), false, true);
                    calEvent.setMinDate(endDate.getTime());
                }
                updateRegistrationText(textRegPeriod);
            }

            if (viewModel.eventDate != null && !viewModel.eventDate.isEmpty()) {
                selectedEventDate = viewModel.eventDate;
                Date eventDate = sdf.parse(selectedEventDate);

                if (eventDate != null) {
                    calEvent.setDate(eventDate.getTime(), false, true);
                }
                textEvent.setText("Official event date: " + selectedEventDate);
                textEvent.setTextColor(Color.parseColor("#888888"));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}