package com.example.zephyrevents.controller;

import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.model.Status;
import com.example.zephyrevents.model.WaitlistEntry;
import com.example.zephyrevents.repository.EventRepository;
import com.example.zephyrevents.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class EventController {

    private static volatile EventController instance;
    private EventRepository eventRepository;

    private List<WaitlistEntry> mockLotteries = new ArrayList<>();
    private List<WaitlistEntry> mockHistory = new ArrayList<>();

    private EventController() {
        eventRepository = new EventRepository();
    }

    public static EventController getInstance() {
        if (instance == null) {
            synchronized (EventController.class) {
                if (instance == null) {
                    instance = new EventController();
                }
            }
        }
        return instance;
    }

    public void createEvent(Event event, RepositoryCallback<Void> callback) {
        eventRepository.saveEvent(event, callback);
    }

    public void deleteEvent(String eventId, RepositoryCallback<Void> callback) {
        eventRepository.deleteEvent(eventId, callback);
    }

    public void getAllEvents(RepositoryCallback<List<Event>> callback) {
        eventRepository.getAllEvents(callback);
    }

    public void getEventById(String eventId, RepositoryCallback<Event> callback) {
        eventRepository.getEventById(eventId, callback);
    }

    // --- UPDATED: WAITLIST SIMULATOR METHODS NOW USE USER ID ---

    public boolean isOnWaitlist(String eventKey, String userId) {
        for (WaitlistEntry entry : mockLotteries) {
            if (entry.getEventId() != null && entry.getEventId().equals(eventKey) &&
                    entry.getUserId() != null && entry.getUserId().equals(userId)) {
                return true;
            }
        }
        return false;
    }

    public void addToWaitlist(String eventKey, String userId) {
        if (!isOnWaitlist(eventKey, userId)) {
            // Using your actual WaitlistEntry constructor!
            // Passing 0.0 for lat/lng for now until map integration is ready
            WaitlistEntry entry = new WaitlistEntry(userId, eventKey, 0.0, 0.0, Status.WAITLISTED);
            mockLotteries.add(entry);
        }
    }

    public void removeFromWaitlist(String eventKey, String userId) {
        mockLotteries.removeIf(e -> e.getEventId() != null && e.getEventId().equals(eventKey) &&
                e.getUserId() != null && e.getUserId().equals(userId));
    }

    public List<WaitlistEntry> getLotteryEntries() {
        return mockLotteries;
    }

    public List<WaitlistEntry> getHistoryEntries() {
        return mockHistory;
    }

    public void addDeclinedEvent(String eventKey, String userId) {
        removeFromWaitlist(eventKey, userId);
        WaitlistEntry entry = new WaitlistEntry(userId, eventKey, 0.0, 0.0, Status.DECLINED);
        mockHistory.add(entry);
    }

    public boolean isInvitedEvent(String eventKey) { return false; }

}