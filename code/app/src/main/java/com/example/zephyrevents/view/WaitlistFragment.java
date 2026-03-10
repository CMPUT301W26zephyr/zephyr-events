package com.example.zephyrevents.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zephyrevents.R;
import com.example.zephyrevents.model.Entrant;

import java.util.ArrayList;
import java.util.List;

public class WaitlistFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_waitlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnNotify = view.findViewById(R.id.btn_notify);
        if (btnNotify != null) btnNotify.setText("NOTIFY");

        Button btnDraw = view.findViewById(R.id.btn_draw);
        if (btnDraw != null) {
            btnDraw.setText("DRAW REPLACEMENTS");
            btnDraw.setOnClickListener(v -> {
                new DrawReplacementsDialog().show(getParentFragmentManager(), "DRAW_DIALOG");
            });
        }

        RecyclerView recyclerView = view.findViewById(R.id.recycler_waitlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<Entrant> mockWaitlist = new ArrayList<>();
        mockWaitlist.add(new Entrant("Jane Doe", "Sunnyville", false));
        mockWaitlist.add(new Entrant("Saad Mohiuddin", "Edmonton", false));
        mockWaitlist.add(new Entrant("Eren Yeager", "Shiganshina", false));
        mockWaitlist.add(new Entrant("Mikasa Ackerman", "Shiganshina", false));
        mockWaitlist.add(new Entrant("Armin Arlelt", "Shiganshina", false));
        mockWaitlist.add(new Entrant("Edward Elric", "Amestris", false));
        mockWaitlist.add(new Entrant("Light Yagami", "Tokyo", false));
        mockWaitlist.add(new Entrant("Violet Evergarden", "Leiden", false));

        EntrantAdapter adapter = new EntrantAdapter(mockWaitlist);
        recyclerView.setAdapter(adapter);
    }
}