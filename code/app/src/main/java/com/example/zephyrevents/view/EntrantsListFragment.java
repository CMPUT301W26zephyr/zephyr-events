package com.example.zephyrevents.view;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
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
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.controller.LotteryController;
import com.example.zephyrevents.controller.NotificationController;
import com.example.zephyrevents.model.Entrant;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.model.Status;
import com.example.zephyrevents.model.User;
import com.example.zephyrevents.model.WaitlistEntry;
import com.example.zephyrevents.repository.EventRepository;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.repository.UserRepository;
import com.example.zephyrevents.repository.WaitlistRepository;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntrantsListFragment extends Fragment {

    private int tabIndex;
    private String eventId;
    private String eventName;
    private WaitlistRepository waitlistRepository;
    private UserRepository userRepository;
    private NotificationController notificationController;
    private List<WaitlistEntry> currentFilteredList = new ArrayList<>();
    private com.google.firebase.firestore.ListenerRegistration eventRegistration;
    private com.google.firebase.firestore.ListenerRegistration waitlistRegistration;
    private Map<String, User> userCache = new HashMap<>();

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
        notificationController = new NotificationController(); // Initialize the controller

        RecyclerView recyclerView = view.findViewById(R.id.recycler_entrants);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        Button btnNotify = view.findViewById(R.id.btn_notify);
        Button btnDrawReplacements = view.findViewById(R.id.btn_draw_replacements);
        Button btnRunLottery = view.findViewById(R.id.btn_run_lottery);
        Button btnExportCsv = view.findViewById(R.id.btn_export_csv);

        if (tabIndex == 0) {
            btnNotify.setVisibility(View.VISIBLE);
            btnRunLottery.setVisibility(View.VISIBLE);
            btnDrawReplacements.setVisibility(View.GONE);

            // Pass the eventId to the dialog so it knows which event to draw for
            btnDrawReplacements.setOnClickListener(v -> {
                DrawReplacementsDialog.newInstance(eventId).show(getParentFragmentManager(), "DRAW_DIALOG");
            });

            btnRunLottery.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Running Lottery...", Toast.LENGTH_SHORT).show();
                new LotteryController().runLottery(eventId, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(requireContext(), "Lottery Complete!", Toast.LENGTH_SHORT).show();

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

        } else if (tabIndex == 1) {
            btnNotify.setVisibility(View.VISIBLE);
        } else if (tabIndex == 2) {
            btnNotify.setVisibility(View.VISIBLE);
        } else if (tabIndex == 3) {
            btnNotify.setVisibility(View.VISIBLE);
            btnExportCsv.setVisibility(View.VISIBLE);
            btnExportCsv.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Exporting CSV...", Toast.LENGTH_SHORT).show();
                saveEntrantsToCsv(requireContext(), currentFilteredList, eventId, userCache);
            });
        }

        btnNotify.setOnClickListener(v -> {
            if (currentFilteredList == null || currentFilteredList.isEmpty()) {
                Toast.makeText(requireContext(), "No users in this list to notify.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Inflate the custom themed XML layout
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_notify_message, null);
            com.google.android.material.textfield.TextInputEditText input = dialogView.findViewById(R.id.et_notify_message);

            // Build the dialog
            androidx.appcompat.app.AlertDialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Send Notification")
                    .setView(dialogView)
                    .setPositiveButton("Send", (d, which) -> {
                        String msg = input.getText() != null ? input.getText().toString().trim() : "";
                        if (msg.isEmpty()) {
                            Toast.makeText(requireContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Get all user IDs currently visible in this tab
                        java.util.List<String> targetIds = new java.util.ArrayList<>();
                        for (WaitlistEntry entry : currentFilteredList) {
                            targetIds.add(entry.getUserId());
                        }

                        // Send the manual notification
                        notificationController.notifyUsersWithCustomMessage(targetIds, eventId, msg);
                        Toast.makeText(requireContext(), "Notifications successfully sent!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .create();

            dialog.show();

            // Color the dialog buttons to match the app's red theme
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.primary_red));
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_secondary));
        });

        loadData(recyclerView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (eventRegistration != null) eventRegistration.remove();
        if (waitlistRegistration != null) waitlistRegistration.remove();
    }

    private void loadData(RecyclerView recyclerView) {
        if (eventRegistration != null) eventRegistration.remove();
        eventRegistration = EventController.getInstance().listenToEventById(eventId, new RepositoryCallback<Event>() {
            @Override
            public void onSuccess(Event event) {
                if (waitlistRegistration != null) waitlistRegistration.remove();
                waitlistRegistration = waitlistRepository.listenToWaitlist(eventId, new RepositoryCallback<List<WaitlistEntry>>() {
                    @Override
                    public void onSuccess(List<WaitlistEntry> result) {
                        List<WaitlistEntry> filtered = new ArrayList<>();

                        // Check if the event is already officially CLOSED
                        boolean lotteryHasRun = false;
                        if (event != null && event.getStatus() == com.example.zephyrevents.model.EventStatus.CLOSED) {
                            lotteryHasRun = true;
                        }

                        for (WaitlistEntry e : result) {
                            if (e.getStatus() == Status.SELECTED || e.getStatus() == Status.ACCEPTED ||
                                    e.getStatus() == Status.DECLINED || e.getStatus() == Status.LOST) {
                                lotteryHasRun = true;
                            }

                            if (tabIndex == 0 && e.getStatus() == Status.WAITLISTED) filtered.add(e);
                            if (tabIndex == 1 && (e.getStatus() == Status.SELECTED || e.getStatus() == Status.ACCEPTED || e.getStatus() == Status.DECLINED)) filtered.add(e);
                            if (tabIndex == 2 && e.getStatus() == Status.SELECTED) filtered.add(e);
                            if (tabIndex == 3 && e.getStatus() == Status.ACCEPTED) filtered.add(e);
                        }

                        currentFilteredList.clear();
                        currentFilteredList.addAll(filtered);

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
                        Map<Entrant, WaitlistEntry> entryMap = new HashMap<>();

                        EntrantAdapter adapter = new EntrantAdapter(entrantList, entrant -> {
                            WaitlistEntry mappedEntry = entryMap.get(entrant);
                            if (mappedEntry != null) {
                                waitlistRepository.updateStatus(eventId, mappedEntry.getUserId(), Status.DECLINED, new RepositoryCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        loadData(recyclerView);
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Toast.makeText(requireContext(), "Failed to cancel entrant", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });

                        recyclerView.setAdapter(adapter);

                        for (WaitlistEntry entry : filtered) {
                            userRepository.getUserById(entry.getUserId(), new RepositoryCallback<User>() {
                                @Override
                                public void onSuccess(User user) {
                                    String name = (user != null && user.getName() != null) ? user.getName() : "Unknown User";
                                    boolean showCancel = (tabIndex == 2);
                                    Entrant entrant = new Entrant(name, "Status: " + entry.getStatus().name(), showCancel);
                                    entryMap.put(entrant, entry);
                                    entrantList.add(entrant);
                                    adapter.notifyDataSetChanged();
                                }
                                @Override
                                public void onFailure(Exception e) {
                                    boolean showCancel = (tabIndex == 2);
                                    Entrant entrant = new Entrant("Unknown User", "Status: " + entry.getStatus().name(), showCancel);
                                    entryMap.put(entrant, entry);
                                    entrantList.add(entrant);
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

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Error checking event status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void saveEntrantsToCsv(Context context, List<WaitlistEntry> entrants, String eventId, Map<String, User> userCache) {
        EventRepository eventRepository = new EventRepository();

        eventRepository.getEventById(eventId, new RepositoryCallback<Event>() {
            @Override
            public void onSuccess(Event result) {
                String eventName = result.getName() != null ? result.getName().strip() : "Unnamed Event";

                String filename = "Entrants_" + eventName + "_" + eventId + "_" + System.currentTimeMillis() + ".csv";
                OutputStream fos;

                try {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
                    values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");

                    // Folder path
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.put(MediaStore.Downloads.RELATIVE_PATH, "Download/ZephyrEvents");
                        values.put(MediaStore.Downloads.IS_PENDING, 1);
                    }

                    Uri uri = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                    }

                    if (uri != null) {
                        fos = context.getContentResolver().openOutputStream(uri);
                        if (fos != null) {

                            // build the file
                            String header = "User ID,Name,Email,Phone,Status\n";  // header
                            fos.write(header.getBytes(StandardCharsets.UTF_8));
                            for (WaitlistEntry entry : entrants) {

                                User user = userCache.get(entry.getUserId());

                                String name = "";
                                String email = "";
                                String phone = "";

                                if (user != null) {
                                    name = user.getName() != null ? user.getName() : "";
                                    if (user.getContactInfo() != null) {
                                        email = user.getContactInfo().getEmail() != null ? user.getContactInfo().getEmail() : "";
                                        phone = user.getContactInfo().getPhone() != null ? user.getContactInfo().getPhone() : "";
                                    }
                                }

                                String row = String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",  // Format
                                        entry.getUserId(),
                                        name.replace("\"", "\"\""),
                                        email.replace("\"", "\"\""),
                                        phone.replace("\"", "\"\""),
                                        entry.getStatus().name()
                                );

                                fos.write(row.getBytes(StandardCharsets.UTF_8));
                            }
                            fos.flush();
                            fos.close();
                        }

                        values.clear();
                        values.put(MediaStore.Downloads.IS_PENDING, 0);  // mark as complete download
                        context.getContentResolver().update(uri, values, null, null);

                        new Handler(Looper.getMainLooper()).post(() ->  // needs to go back to view thread
                                Toast.makeText(context, "CSV saved to Downloads/ZephyrEvents", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    new Handler(Looper.getMainLooper()).post(() ->  // needs to go back to view thread
                            Toast.makeText(context, "Failed to save CSV", Toast.LENGTH_SHORT).show());
                }

            }
            @Override
            public void onFailure(Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->  // needs to go back to view thread
                        Toast.makeText(context, "Retrieve Event", Toast.LENGTH_SHORT).show());
            }
        });

    }
}