package com.example.zephyrevents.controller;

import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.model.NotificationType;
import com.example.zephyrevents.model.Status;
import com.example.zephyrevents.model.WaitlistEntry;
import com.example.zephyrevents.repository.EventRepository;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.repository.WaitlistRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controller that manages logic related to the event lottery system.
 * Handles random selection of winners from an event's waitlist.
 */
public class LotteryController {

    private final WaitlistRepository waitlistRepository;
    private final EventRepository eventRepository;
    private final NotificationController notificationController;

    public LotteryController() {
        waitlistRepository = new WaitlistRepository();
        eventRepository = new EventRepository();
        notificationController = new NotificationController();
    }

    // Constructor for unit testing
    @androidx.annotation.VisibleForTesting
    public LotteryController(EventRepository event, WaitlistRepository waitlist) {
        this.waitlistRepository = waitlist;
        this.eventRepository = event;
        this.notificationController = new NotificationController();
    }

    // Added a callback so the UI knows when the lottery finishes saving!

    /**
     * Executes the lottery process for a specific event.
     * - Fetches event and all Waitlist entries for that event
     * - Filters entries for eligible entrants (Status.WAITLISTED)
     * - Shuffles pool and selects winners and losers (Status.LOST)
     *
     * @param eventId   Unique identifier of the event for run the lottery on.
     * @param callback  A RepositoryCallback to notify when the lottery selection finishes.
     */
    public void runLottery(String eventId, RepositoryCallback<Void> callback) {
        eventRepository.getEventById(eventId, new RepositoryCallback<Event>() {
            @Override
            public void onSuccess(Event event) {
                int capacity = event.getCapacity();

                waitlistRepository.getWaitlist(eventId, new RepositoryCallback<List<WaitlistEntry>>() {
                    @Override
                    public void onSuccess(List<WaitlistEntry> allEntrants) {

                        List<WaitlistEntry> eligible = new ArrayList<>();
                        for(WaitlistEntry e : allEntrants) {
                            if (e.getStatus() == Status.WAITLISTED) eligible.add(e);
                        }

                        if (eligible.isEmpty()) {
                            if(callback != null) callback.onSuccess(null);
                            return;
                        }

                        Collections.shuffle(eligible);
                        int winnersCount = Math.min(capacity > 0 ? capacity : eligible.size(), eligible.size());
                        List<WaitlistEntry> winners = eligible.subList(0, winnersCount);

                        // Track how many database updates have completed
                        final int totalUpdates = eligible.size();
                        final int[] completedUpdates = {0};

                        for (WaitlistEntry entry : eligible) {
                            boolean isWinner = winners.contains(entry);
                            Status newStatus = winners.contains(entry) ? Status.SELECTED : Status.LOST;

                            NotificationType type = isWinner ? NotificationType.WON_EVENT : NotificationType.LOST_EVENT;
                            String message = isWinner
                                    ? "Congrats! You've been chosen for \"" + event.getName() + "\". Confirm your spot today."
                                    : "The draw for \"" + event.getName() + "\" is complete. You were not selected this time.";

                            notificationController.sendAutomaticNotification(entry.getUserId(), eventId, type, message);

                            // Pass a callback to wait for the update to actually finish saving
                            waitlistRepository.updateStatus(eventId, entry.getUserId(), newStatus, new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    checkCompletion();
                                }
                                @Override
                                public void onFailure(Exception e) {
                                    checkCompletion(); // Proceed anyway to avoid hanging
                                }

                                private void checkCompletion() {
                                    completedUpdates[0]++;
                                    if (completedUpdates[0] == totalUpdates) {
                                        notificationController.sendAutomaticNotification(
                                                event.getOrganizerId(),
                                                eventId,
                                                NotificationType.LOTTERY_COMPLETED,
                                                "The lottery for \"" + event.getName() + "\" has been successfully run."
                                        );
                                        if (callback != null) callback.onSuccess(null);
                                    }
                                }
                            });
                        }
                    }
                    @Override
                    public void onFailure(Exception e) { if(callback!=null) callback.onFailure(e); }
                });
            }
            @Override
            public void onFailure(Exception e) { if(callback!=null) callback.onFailure(e); }
        });
    }
}