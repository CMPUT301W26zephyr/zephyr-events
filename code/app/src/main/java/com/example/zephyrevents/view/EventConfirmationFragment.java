package com.example.zephyrevents.view;

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
import com.example.zephyrevents.model.EventViewModel;

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

        TextView title = view.findViewById(R.id.text_confirm_title);
        TextView type = view.findViewById(R.id.text_confirm_type);
        TextView date = view.findViewById(R.id.text_confirm_date);
        TextView location = view.findViewById(R.id.text_confirm_location);
        TextView price = view.findViewById(R.id.text_confirm_price);
        TextView capacity = view.findViewById(R.id.text_confirm_capacity);
        TextView description = view.findViewById(R.id.text_confirm_description);

        title.setText(viewModel.title);
        type.setText(viewModel.type);
        date.setText(viewModel.eventDate);
        location.setText(viewModel.location);
        price.setText("$" + viewModel.price);
        description.setText(viewModel.description);

        if (viewModel.waitlistCapacity.isEmpty()) {
            capacity.setText(viewModel.attendeeCount + " attendees (No Waitlist)");
        } else {
            capacity.setText(viewModel.attendeeCount + " attendees (" + viewModel.waitlistCapacity + " waitlist)");
        }

        ((OrganizerEventAddEditView) requireActivity()).setupTopAndBottomUI(
                "Review Event Details", "Confirm & Create", v -> {
                    Toast.makeText(requireContext(), "Event Created Successfully!", Toast.LENGTH_SHORT).show();
                    requireActivity().finish();
                }
        );
    }
}