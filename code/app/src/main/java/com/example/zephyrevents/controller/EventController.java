package com.example.zephyrevents.controller;

import com.example.zephyrevents.model.MyEventEntry;
import com.example.zephyrevents.model.User;
import com.example.zephyrevents.model.WaitlistEntry;
import com.example.zephyrevents.util.MyEventsStore;
import com.example.zephyrevents.util.SampleEvents;
import com.example.zephyrevents.model.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for event list/detail and waitlist actions
 * Provides filtered event details to views and manages joining/leaving the waitlist.
 * Collaborators: EventRepository, WaitlistRepository (when implemented); currently uses
 * SampleEvents and MyEventsStore for data.
 */
public class EventController {

    private static volatile EventController instance;

    /** Event keys for use by views (e.g. UI logic like placeholder color). */
    public static final String KEY_SWIMMING = SampleEvents.KEY_SWIMMING;
    public static final String KEY_PIANO = SampleEvents.KEY_PIANO;
    public static final String KEY_YOGA = SampleEvents.KEY_YOGA;

    /** For now uses in-memory/sample data; can be replaced with EventRepository later. */
    private EventController() {}

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
     * Returns the event for the given key, or null if not found.
     * Used by EventDetailView and list item bindings.
     */
    public Event getEvent(String key) {
        return SampleEvents.getEvent(key);
    }

    /**
     * Returns the list of events to show on the main events list (HomePageView / Events ListView).
     */
    public List<Event> getEventsForList() {
        List<Event> list = new ArrayList<>();
        Event e;
        if ((e = SampleEvents.getEvent(SampleEvents.KEY_SWIMMING)) != null) list.add(e);
        if ((e = SampleEvents.getEvent(SampleEvents.KEY_PIANO)) != null) list.add(e);
        if ((e = SampleEvents.getEvent(SampleEvents.KEY_YOGA)) != null) list.add(e);
        return list;
    }

    /**
     * Returns true if the user has joined the waitlist for this event (and not left it).
     */
    public boolean isOnWaitlist(String eventKey) {
        return MyEventsStore.isOnWaitlist(eventKey);
    }

    /**
     * Adds the user to the waitlist for the event (Join Waitlist action).
     */
    public void addToWaitlist(String eventKey) {
        MyEventsStore.addJoinedEvent(eventKey, new User());
    }

    /**
     * Removes the user from the waitlist for the event (Leave Waitlist action).
     */
    public void removeFromWaitlist(String eventKey) {
        MyEventsStore.removeJoinedEvent(eventKey);
    }

    /**
     * Returns entries for the Lotteries tab (events user joined / selected).
     */
    public List<WaitlistEntry> getLotteryEntries() {
        return MyEventsStore.getLotteryEntries();
    }

    /**
     * Returns entries for the History tab (declined invites).
     */
    public List<WaitlistEntry> getHistoryEntries() {
        return MyEventsStore.getHistoryEntries();
    }

    /**
     * Records that the user declined an invite; moves event from Lotteries to History.
     */
    public void addDeclinedEvent(String eventKey) {
        MyEventsStore.addDeclinedEvent(eventKey,new User());
    }

    /**
     * Returns true if this event should show Accept/Decline (user was selected for lottery).
     * Used when opening event detail from list to decide invited state.
     */
    public boolean isInvitedEvent(String eventKey) {
        return SampleEvents.KEY_YOGA.equals(eventKey);
    }
}
