package com.example.zephyrevents.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.LotteryController;
import com.example.zephyrevents.model.Entrant;
import com.example.zephyrevents.model.Status;
import com.example.zephyrevents.model.User;
import com.example.zephyrevents.model.WaitlistEntry;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.repository.UserRepository;
import com.example.zephyrevents.repository.WaitlistRepository;

import java.util.ArrayList;
import java.util.List;

public class EntrantsListFragment extends Fragment {

    private int tabIndex;
    private String eventId;
    private WaitlistRepository waitlistRepository;
    private UserRepository userRepository;

    public static EntrantsListFragment newInstance(int tabIndex, String eventId) {
        EntrantsListFragment fragment = new EntrantsListFragment();
        Bundle args = new Bundle();
        args.putInt("tabIndex", tabIndex);
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrants_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            tabIndex = getArguments().getInt("tabIndex");
            eventId = getArguments().getString("eventId");
        }

        waitlistRepository = new WaitlistRepository();
        userRepository = new UserRepository();

        RecyclerView recyclerView = view.findViewById(R.id.recycler_entrants);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        Button btnNotify = view.findViewById(R.id.btn_notify);
        Button btnDrawReplacements = view.findViewById(R.id.btn_draw_replacements);
        Button btnRunLottery = view.findViewById(R.id.btn_run_lottery);
        Button btnExportCsv = view.findViewById(R.id.btn_export_csv);

        // 1. Manually set the text for your buttons here
        btnNotify.setText("Notify");
        btnDrawReplacements.setText("Draw Replacements");
        btnRunLottery.setText("Run Lottery");
        btnExportCsv.setText("Export CSV");

        // Turn on buttons based on the current tab
        if (tabIndex == 0) { // Waitlist
            btnNotify.setVisibility(View.VISIBLE);
            btnRunLottery.setVisibility(View.VISIBLE);

            // Ensure Draw Replacements is hidden initially (max 2 buttons)
            btnDrawReplacements.setVisibility(View.GONE);

            btnDrawReplacements.setOnClickListener(v -> {
                // Calls your dialog
                new DrawReplacementsDialog().show(getParentFragmentManager(), "DRAW_DIALOG");
            });

            btnRunLottery.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Running Lottery...", Toast.LENGTH_SHORT).show();
                new LotteryController().runLottery(eventId, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(requireContext(), "Lottery Complete!", Toast.LENGTH_SHORT).show();

                        // 2. Swap the buttons upon successful lottery run
                        btnRunLottery.setVisibility(View.GONE);
                        btnDrawReplacements.setVisibility(View.VISIBLE);

                        loadData(recyclerView);
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(requireContext(), "Failed to run lottery.", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            btnNotify.setOnClickListener(v -> Toast.makeText(requireContext(), "Notify Waitlist Clicked", Toast.LENGTH_SHORT).show());

        } else if (tabIndex == 1) { // Winners
            btnNotify.setVisibility(View.VISIBLE);
            btnNotify.setOnClickListener(v -> Toast.makeText(requireContext(), "Notify Winners Clicked", Toast.LENGTH_SHORT).show());

        } else if (tabIndex == 2) { // Unregistered
            btnNotify.setVisibility(View.VISIBLE);
            btnNotify.setOnClickListener(v -> Toast.makeText(requireContext(), "Notify Unregistered Clicked", Toast.LENGTH_SHORT).show());

        } else if (tabIndex == 3) { // Final List
            btnNotify.setVisibility(View.VISIBLE);
            btnExportCsv.setVisibility(View.VISIBLE);

            btnNotify.setOnClickListener(v -> Toast.makeText(requireContext(), "Notify Final List Clicked", Toast.LENGTH_SHORT).show());
            btnExportCsv.setOnClickListener(v -> Toast.makeText(requireContext(), "Exporting CSV...", Toast.LENGTH_SHORT).show());
        }

        loadData(recyclerView);
    }

    private void loadData(RecyclerView recyclerView) {
        waitlistRepository.getWaitlist(eventId, new RepositoryCallback<List<WaitlistEntry>>() {
            @Override
            public void onSuccess(List<WaitlistEntry> result) {
                List<WaitlistEntry> filtered = new ArrayList<>();
                boolean lotteryHasRun = false;

                for (WaitlistEntry e : result) {
                    // If anyone has a post-lottery status, we know the lottery ran
                    if (e.getStatus() == Status.SELECTED || e.getStatus() == Status.ACCEPTED ||
                            e.getStatus() == Status.DECLINED || e.getStatus() == Status.LOST) {
                        lotteryHasRun = true;
                    }

                    if (tabIndex == 0 && e.getStatus() == Status.WAITLISTED) filtered.add(e);
                    if (tabIndex == 1 && (e.getStatus() == Status.SELECTED || e.getStatus() == Status.ACCEPTED)) filtered.add(e);
                    if (tabIndex == 2 && e.getStatus() == Status.SELECTED) filtered.add(e);
                    if (tabIndex == 3 && e.getStatus() == Status.ACCEPTED) filtered.add(e);
                }

                // Handle persistent button states based on database reality
                if (tabIndex == 0 && getView() != null) {
                    Button btnRunLottery = getView().findViewById(R.id.btn_run_lottery);
                    Button btnDrawReplacements = getView().findViewById(R.id.btn_draw_replacements);

                    if (lotteryHasRun) {
                        btnRunLottery.setVisibility(View.GONE);
                        btnDrawReplacements.setVisibility(View.VISIBLE);
                    } else {
                        btnRunLottery.setVisibility(View.VISIBLE);
                        btnDrawReplacements.setVisibility(View.GONE);
                    }
                }

                List<Entrant> entrantList = new ArrayList<>();
                EntrantAdapter adapter = new EntrantAdapter(entrantList);
                recyclerView.setAdapter(adapter);

                for (WaitlistEntry entry : filtered) {
                    userRepository.getUserById(entry.getUserId(), new RepositoryCallback<User>() {
                        @Override
                        public void onSuccess(User user) {
                            String name = (user != null && user.getName() != null) ? user.getName() : "Unknown User";
                            entrantList.add(new Entrant(name, "Status: " + entry.getStatus().name(), false));
                            adapter.notifyDataSetChanged();
                        }
                        @Override
                        public void onFailure(Exception e) {
                            entrantList.add(new Entrant("Unknown User", "Status: " + entry.getStatus().name(), false));
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Error loading waitlist", Toast.LENGTH_SHORT).show();
            }
        });
    }
}