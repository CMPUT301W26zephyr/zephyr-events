package com.example.zephyrevents.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.zephyrevents.R;

public class UnregisteredFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_unregistered, container, false);

        try {
            ViewGroup linearLayout = (ViewGroup) ((ViewGroup) view).getChildAt(0);
            ViewGroup mockList = (ViewGroup) linearLayout.getChildAt(0);
            for (int i = 0; i < mockList.getChildCount(); i++) {
                View card = mockList.getChildAt(i);
                View cancelBtn = card.findViewById(R.id.btn_cancel_entrant);
                if (cancelBtn != null) {
                    cancelBtn.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            // Ignore for mock
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnNotify = view.findViewById(R.id.btn_notify);
        if (btnNotify != null) btnNotify.setText("NOTIFY");
    }
}