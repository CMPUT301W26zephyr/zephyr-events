package com.example.zephyrevents.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.zephyrevents.R;
import com.example.zephyrevents.model.EventViewModel;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class EventLocationFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EventViewModel viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        EditText inputLocation = view.findViewById(R.id.input_location);
        EditText inputAddress = view.findViewById(R.id.input_address);
        SwitchMaterial switchGeo = view.findViewById(R.id.switch_geolocation);

        inputLocation.setText(viewModel.location);
        switchGeo.setChecked(viewModel.requireGeolocation);

        ((OrganizerEventAddEditView) requireActivity()).setupTopAndBottomUI(
                "Create Event", "NEXT", v -> {
                    if (inputLocation.getText().toString().trim().isEmpty()) {
                        inputLocation.setError("Location is required");
                    } else {
                        viewModel.location = inputLocation.getText().toString().trim();
                        viewModel.address = inputAddress.getText().toString().trim();
                        viewModel.requireGeolocation = switchGeo.isChecked();
                        ((OrganizerEventAddEditView) requireActivity()).navigateToFragment(new EventDatesFragment(), true);
                    }
                }
        );
    }
}