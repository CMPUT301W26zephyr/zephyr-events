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

        // Required Fields (Always Display)
        TextView title = view.findViewById(R.id.text_confirm_title);
        TextView date = view.findViewById(R.id.text_confirm_date);
        TextView regPeriod = view.findViewById(R.id.text_confirm_reg_period);
        TextView location = view.findViewById(R.id.text_confirm_location);
        TextView geo = view.findViewById(R.id.text_confirm_geo);
        TextView capacity = view.findViewById(R.id.text_confirm_capacity);

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
                    Toast.makeText(requireContext(), "Event Created Successfully!", Toast.LENGTH_SHORT).show();
                    requireActivity().finish();
                }
        );
    }
}