package com.example.zephyrevents;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class EventLocationFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.toolbar_back).setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> requireActivity().finish());

        Button nextBtn = view.findViewById(R.id.next_button);
        nextBtn.setText("Next");
        nextBtn.setOnClickListener(v -> {
            ((OrganizerEventAddEditView) requireActivity())
                    .navigateToFragment(new EventDatesFragment(), true);
        });
    }
}