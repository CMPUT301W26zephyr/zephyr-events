package com.example.zephyrevents;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventDatesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_dates, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button nextBtn = view.findViewById(R.id.next_button);
        nextBtn.setText("Next");
        nextBtn.setOnClickListener(v -> {
            ((OrganizerEventAddEditView) requireActivity()).navigateToFragment(new com.example.zephyrevents.EventConfirmationFragment(), true);
        });

        view.findViewById(R.id.toolbar_back).setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> requireActivity().finish());

        EditText inputRegStart = view.findViewById(R.id.input_reg_start_date);
        inputRegStart.setOnClickListener(v -> showDatePicker(inputRegStart));

        EditText inputEventStart = view.findViewById(R.id.input_event_start_date);
        inputEventStart.setOnClickListener(v -> showDatePicker(inputEventStart));

        view.findViewById(R.id.next_button).setOnClickListener(v -> {
            ((OrganizerEventAddEditView) requireActivity()).navigateToFragment(new EventConfirmationFragment(), true);
        });
    }

    private void showDatePicker(EditText targetInput) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String dateString = sdf.format(new Date(selection));
            targetInput.setText(dateString);
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }
}
