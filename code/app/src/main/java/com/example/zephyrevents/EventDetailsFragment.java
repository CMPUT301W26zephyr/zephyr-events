package com.example.zephyrevents;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class EventDetailsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Make sure this matches your XML file name!
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.toolbar_back).setOnClickListener(v -> requireActivity().finish());
        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> requireActivity().finish());

        String[] eventTypes = new String[]{"Class", "Workshop", "Kids Program"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                eventTypes
        );
        AutoCompleteTextView dropdown = view.findViewById(R.id.eventTypeDropdown);
        dropdown.setAdapter(adapter);

        Button nextBtn = view.findViewById(R.id.next_button);
        nextBtn.setText("Next");
        nextBtn.setOnClickListener(v -> {
            ((OrganizerEventAddEditView) requireActivity())
                    .navigateToFragment(new EventLocationFragment(), true);
        });
    }
}