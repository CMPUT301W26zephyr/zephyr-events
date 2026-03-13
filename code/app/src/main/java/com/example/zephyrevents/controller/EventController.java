package com.example.zephyrevents.controller;

import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.model.Status;
import com.example.zephyrevents.model.WaitlistEntry;
import com.example.zephyrevents.repository.EventRepository;
import com.example.zephyrevents.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

// TODO: Once no longer mocked, I recommend to convert into non-singleton class for consistency - Jason
/**
 * Singleton controller managing event data and waitlist.
 * NOTE: Currently waitlisting is mocked/simulated in local memory. Singleton only needed due to this.
 * Acts as a centralized access pointe
 */
public class EventController {

    private static volatile EventController instance;
    private EventRepository eventRepository;

    private List<WaitlistEntry> mockLotteries = new ArrayList<>();
    private List<WaitlistEntry> mockHistory = new ArrayList<>();

    /**
     * Private constructor to enforce the Singleton pattern.
     */
    private EventController() {
        eventRepository = new EventRepository();
    }

    /**
     * Retrieves the Singleton instance of the EventController
     * @return The EventController instance.
     */
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

    /**
     * Saves new event or updates existing one via the repository.
     * @param event     The Event object to save.
     * @param callback  Notified when save operation completed.
     */
    public void createEvent(Event event, RepositoryCallback<Void> callback) {
        eventRepository.saveEvent(event, callback);
    }

    /**
     * Deletes an event from the repository.
     * @param eventId   The ID of the event to remove.
     * @param callback  Notified when deletion completed.
     */
    public void deleteEvent(String eventId, RepositoryCallback<Void> callback) {
        eventRepository.deleteEvent(eventId, callback);
    }

    /**
     * Fetches all events from the EventRepository.
     * @param callback  Returns a list of all Event objects.
     */
    public void getAllEvents(RepositoryCallback<List<Event>> callback) {
        eventRepository.getAllEvents(callback);
    }

    /**
     * Retrieves a single event by its unique ID.
     * @param eventId   The ID of the event to fetch.
     * @param callback  Returns the Event object if found.
     */
    public void getEventById(String eventId, RepositoryCallback<Event> callback) {
        eventRepository.getEventById(eventId, callback);
    }

    // --- UPDATED: WAITLIST SIMULATOR METHODS NOW USE USER ID ---

    /**
     * Checks if user is in the mock waitlist for specific event.
     * @param eventKey  event ID
     * @param userId    user ID
     * @return  true if user is in mock waitlist, false otherwise
     */
    public boolean isOnWaitlist(String eventKey, String userId) {
        for (WaitlistEntry entry : mockLotteries) {
            if (entry.getEventId() != null && entry.getEventId().equals(eventKey) &&
                    entry.getUserId() != null && entry.getUserId().equals(userId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a user to mock waitlist with status.WAITLISTED.
     * @param eventKey  event ID
     * @param userId    user ID
     */
    public void addToWaitlist(String eventKey, String userId) {
        if (!isOnWaitlist(eventKey, userId)) {
            // TODO: Passing 0.0 for lat/lng for now until map integration is ready
            WaitlistEntry entry = new WaitlistEntry(userId, eventKey, 0.0, 0.0, Status.WAITLISTED);
            mockLotteries.add(entry);
        }
    }

    /**
     * Removes a user from mock waitlist based on ID.
     * @param eventKey  event ID
     * @param userId    user ID
     */
    public void removeFromWaitlist(String eventKey, String userId) {
        mockLotteries.removeIf(e -> e.getEventId() != null && e.getEventId().equals(eventKey) &&
                e.getUserId() != null && e.getUserId().equals(userId));
    }

    /**
     * Retrieves the current list of entries in the mock lottery system.
     * @return List of all mock WaitlistEntry objects
     */
    public List<WaitlistEntry> getLotteryEntries() {
        return mockLotteries;
    }

    /**
     * Retrieves the history of finalized entries (declined or completed).
     * @return A list of historical {@link WaitlistEntry} objects.
     */
    public List<WaitlistEntry> getHistoryEntries() {
        return mockHistory;
    }

    /**
     * Moves a user from the waitlist to the history list with status.DECLINED.
     * @param eventKey  event ID
     * @param userId    user ID
     */
    public void addDeclinedEvent(String eventKey, String userId) {
        removeFromWaitlist(eventKey, userId);
        WaitlistEntry entry = new WaitlistEntry(userId, eventKey, 0.0, 0.0, Status.DECLINED);
        mockHistory.add(entry);
    }

    /**
     * Determines if a user has been invited to a specific event.
     *
     * @param eventKey  event ID.
     * @return  Currently returns false (placeholder).
     */
    public boolean isInvitedEvent(String eventKey) { return false; }

}