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

public class WinnersFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_winners, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnNotify = view.findViewById(R.id.btn_notify);
        if (btnNotify != null) btnNotify.setText("NOTIFY");

        RecyclerView recyclerView = view.findViewById(R.id.recycler_winners);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<Entrant> mockWaitlist = new ArrayList<>();
        mockWaitlist.add(new Entrant("Hornet", "Pharloom", false));
        mockWaitlist.add(new Entrant("Hollow Knight", "Hallownest", false));
        mockWaitlist.add(new Entrant("Ghost", "Hallownest", false));
        mockWaitlist.add(new Entrant("Elderbug", "Dirtmouth", false));
        mockWaitlist.add(new Entrant("Godseeker", "Godhome", false));

        EntrantAdapter adapter = new EntrantAdapter(mockWaitlist);
        recyclerView.setAdapter(adapter);
    }
}