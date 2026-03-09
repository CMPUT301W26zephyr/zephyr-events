package com.example.zephyrevents.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.zephyrevents.R;
import com.example.zephyrevents.model.EventViewModel;

public class EventDetailsFragment extends Fragment {

    private EventViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        EditText inputTitle = view.findViewById(R.id.input_event_title);
        AutoCompleteTextView dropdownType = view.findViewById(R.id.eventTypeDropdown);
        EditText inputPrice = view.findViewById(R.id.input_event_price);
        EditText inputDesc = view.findViewById(R.id.input_event_desc);
        EditText inputWaitlist = view.findViewById(R.id.input_waitlist);
        EditText inputAttendeeCount = view.findViewById(R.id.input_attendee_count);

        inputTitle.setText(viewModel.title);
        dropdownType.setText(viewModel.type, false);
        inputPrice.setText(viewModel.price);
        inputDesc.setText(viewModel.description);
        inputWaitlist.setText(viewModel.waitlistCapacity);
        inputAttendeeCount.setText(viewModel.attendeeCount);

        String[] eventTypes = new String[]{"Educational", "Workshop", "Corporate", "Social", "Recreation", "Entertainment", "Networking", "Other"};
        dropdownType.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, eventTypes));

        ((OrganizerEventAddEditView) requireActivity()).setupTopAndBottomUI(
                "Create Event", "Next", v -> {
                    boolean isValid = true;

                    if (inputTitle.getText().toString().trim().isEmpty()) {
                        inputTitle.setError("Required");
                        isValid = false;
                    }
                    if (inputAttendeeCount.getText().toString().trim().isEmpty()) {
                        inputAttendeeCount.setError("Required");
                        isValid = false;
                    }

                    if (isValid) {
                        viewModel.title = inputTitle.getText().toString().trim();
                        viewModel.type = dropdownType.getText().toString().trim();
                        viewModel.price = inputPrice.getText().toString().trim();
                        viewModel.description = inputDesc.getText().toString().trim();
                        viewModel.waitlistCapacity = inputWaitlist.getText().toString().trim();
                        viewModel.attendeeCount = inputAttendeeCount.getText().toString().trim();

                        ((OrganizerEventAddEditView) requireActivity()).navigateToFragment(new EventLocationFragment(), true);
                    }
                }
        );
    }
}