package com.example.zephyrevents.controller;

import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.repository.EventRepository;
import com.example.zephyrevents.repository.RepositoryCallback;

import java.util.List;

/**
 * Controller for bridging the UI with the backend Repositories.
 */
public class EventController {

    private static volatile EventController instance;
    private EventRepository eventRepository;

    private EventController() {
        // Initialize the real repository!
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

    // --- REAL BACKEND METHODS ---

    public void createEvent(Event event, RepositoryCallback<Void> callback) {
        eventRepository.saveEvent(event, callback);
    }

    public void getAllEvents(RepositoryCallback<List<Event>> callback) {
        eventRepository.getAllEvents(callback);
    }

    public void getEventById(String eventId, RepositoryCallback<Event> callback) {
        eventRepository.getEventById(eventId, callback);
    }

    // --- STUBS FOR LATER (To prevent your code from crashing for now) ---
    public boolean isInvitedEvent(String eventKey) { return false; }
    public boolean isOnWaitlist(String eventKey) { return false; }
    public void addToWaitlist(String eventKey) {}
    public void removeFromWaitlist(String eventKey) {}
    public void addDeclinedEvent(String eventKey) {}
}