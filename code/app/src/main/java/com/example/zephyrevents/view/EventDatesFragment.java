package com.example.zephyrevents.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.zephyrevents.R;
import com.example.zephyrevents.model.EventViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventDatesFragment extends Fragment {

    private EventViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_dates, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        EditText inputRegPeriod = view.findViewById(R.id.input_registration_period);
        CalendarView inlineCalendar = view.findViewById(R.id.inline_event_calendar);
        TextView eventDateText = view.findViewById(R.id.text_selected_event_date);

        inputRegPeriod.setText(viewModel.registrationPeriod);
        if (!viewModel.eventDate.isEmpty()) {
            eventDateText.setText("Your event is scheduled for " + viewModel.eventDate);
        }

        inputRegPeriod.setOnClickListener(v -> showDateRangePicker(inputRegPeriod));

        inlineCalendar.setOnDateChangeListener((calView, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            viewModel.eventDate = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(selectedDate.getTime());
            eventDateText.setText("Your event is scheduled for " + viewModel.eventDate);
        });

        ((OrganizerEventAddEditView) requireActivity()).setupTopAndBottomUI(
                "Create Event", "Next", v -> {
                    boolean isValid = true;
                    if (inputRegPeriod.getText().toString().isEmpty()) { inputRegPeriod.setError("Required"); isValid = false; }
                    if (viewModel.eventDate.isEmpty()) {
                        eventDateText.setText("Please select a date above!"); eventDateText.setTextColor(getResources().getColor(android.R.color.holo_red_dark)); isValid = false; }

                    if (isValid) {
                        ((OrganizerEventAddEditView) requireActivity()).navigateToFragment(new EventConfirmationFragment(), true);
                    }
                }
        );
    }

    private void showDateRangePicker(EditText targetInput) {
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker().setTitleText("Select Registration Period").build();
        picker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            viewModel.registrationPeriod = sdf.format(new Date(selection.first)) + " - " + sdf.format(new Date(selection.second));
            targetInput.setText(viewModel.registrationPeriod);
            targetInput.setError(null);
        });
        picker.show(getParentFragmentManager(), "DATE_RANGE_PICKER");
    }
}