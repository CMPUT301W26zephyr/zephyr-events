package com.example.zephyrevents.controller;

import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.model.NotificationType;
import com.example.zephyrevents.model.WaitlistEntry;
import com.example.zephyrevents.repository.EventRepository;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.repository.WaitlistRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LotteryController {

    private final WaitlistRepository waitlistRepository;
    private final EventRepository eventRepository;
    private final NotificationController notificationController;

    //constructor
    public LotteryController() {
        waitlistRepository = new WaitlistRepository();
        eventRepository = new EventRepository();
        notificationController = new NotificationController();
    }
    // when Run Lottery button is clicked,
    //get event id, waitlist, shuffle entrants, select winner -then send notification.
    public void runLottery(String eventId) {

        eventRepository.getEventById(eventId, new RepositoryCallback<Event>() {

            @Override
            public void onSuccess(Event event) {

                int capacity = event.getCapacity();

                waitlistRepository.getWaitlist(eventId, new RepositoryCallback<List<WaitlistEntry>>() {

                    @Override
                    public void onSuccess(List<WaitlistEntry> entrants) {

                        List<WaitlistEntry> shuffled = new ArrayList<>(entrants);
                        Collections.shuffle(shuffled);

                        List<WaitlistEntry> winners =
                                shuffled.subList(0, Math.min(capacity, shuffled.size()));

                        for (WaitlistEntry entry : shuffled) {

                            if (winners.contains(entry)) {

                                notificationController.sendAutomaticNotification(
                                        entry.getUserId(),
                                        eventId,
                                        NotificationType.WON_EVENT,
                                        "You were selected for the event."
                                );

                            } else {

                                notificationController.sendAutomaticNotification(
                                        entry.getUserId(),
                                        eventId,
                                        NotificationType.LOST_EVENT,
                                        "You were not selected for the event."
                                );
                            }
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        System.out.println("Failed to retrieve waitlist");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("Failed to retrieve event");
            }
        });
    }
}