package com.example.zephyrevents.controller;

import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.model.Status;
import com.example.zephyrevents.model.WaitlistEntry;
import com.example.zephyrevents.repository.EventRepository;
import com.example.zephyrevents.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

import android.media.Image;
import android.net.Uri;

import androidx.annotation.VisibleForTesting;

import com.example.zephyrevents.repository.ImageRepository;


// TODO: Once no longer mocked, I recommend to convert into non-singleton class for consistency - Jason
/**
 * Singleton controller managing event data and waitlist.
 * NOTE: Currently waitlisting is mocked/simulated in local memory. Singleton only needed due to this.
 * Acts as a centralized access pointe.
 */
public class EventController {

    private static volatile EventController instance;
    private EventRepository eventRepository;

    private List<WaitlistEntry> mockLotteries = new ArrayList<>();
    private List<WaitlistEntry> mockHistory = new ArrayList<>();

    private ImageRepository imageRepository;

    /**
     * Private constructor to enforce the Singleton pattern.
     */
    private EventController() {
        eventRepository = new EventRepository();
        imageRepository = new ImageRepository();
    }

    /**
     * Unit tests only: inject repository and skip real Firebase / {@link ImageRepository}.
     */
    @VisibleForTesting
    public EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
        this.imageRepository = null;
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
     * if pendingimageUri exist then Upload image to storage and save event with imageUrl.
     * else set imageUrl from the existing image url
     */

    public void saveEventWithOptionalImage(Event event, Uri pendingImageUri, String existingImageUrl, RepositoryCallback<Void> callback ){
        if (pendingImageUri != null){
            imageRepository.uploadEventImage(pendingImageUri, event.getEventId(), new RepositoryCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    event.setImageUrl(result);
                    eventRepository.saveEvent(event, callback);
                }

                @Override
                public void onFailure(Exception e) {
                    callback.onFailure(e);

                }
            });
        } else{
            final String eventId = event.getEventId();
            if (existingImageUrl != null && !existingImageUrl.trim().isEmpty()){
                event.setImageUrl(existingImageUrl.trim());
            } else{
                event.setImageUrl(null);
            }

            final boolean deletePosterInStorage = existingImageUrl == null || existingImageUrl.trim().isEmpty();

            eventRepository.saveEvent(event, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    if (deletePosterInStorage && eventId != null && !eventId.trim().isEmpty()){
                        imageRepository.deleteEventPoster(eventId, new RepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                callback.onSuccess(null);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                callback.onSuccess(null);

                            }
                        });
                    } else {
                        callback.onSuccess(null);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    callback.onFailure(e);

                }
            });
        }
    }

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

    /**
     * Returns document pertaining to a specific event with a real-time snapshot listener
     *
     * @param eventId
     * @param callback
     * @return A listener from firebase
     */
    public com.google.firebase.firestore.ListenerRegistration listenToEventById(String eventId, RepositoryCallback<Event> callback) {
        return eventRepository.listenToEventById(eventId, callback);
    }

}