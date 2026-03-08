package com.example.zephyrevents;

import com.example.zephyrevents.model.EventStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory store of events the user has joined (Join Waitlist).
 * When the user taps "Join Waitlist" on an event, that event is added here
 * so it appears on the My Events → Lotteries tab.
 */
public final class MyEventsStore {

    private static final List<MyEventEntry> lotteryEntries = new CopyOnWriteArrayList<>();
    private static final List<MyEventEntry> declinedEntries = new CopyOnWriteArrayList<>();

    static {
        long oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000L);
        lotteryEntries.add(new MyEventEntry(SampleEvents.KEY_YOGA, EventStatus.SELECTED, oneDayAgo));
    }

    private MyEventsStore() {}

    /**
     * Returns true if the user has joined the waitlist for this event (and not left it).
     */
    public static boolean isOnWaitlist(String eventKey) {
        if (eventKey == null) return false;
        for (MyEventEntry e : lotteryEntries) {
            if (eventKey.equals(e.getEventKey())) return true;
        }
        return false;
    }

    /**
     * Call this when the user taps "Join Waitlist" on an event.
     * Adds the event to the lotteries list with status WAITING (PENDING) if not already present.
     */
    public static void addJoinedEvent(String eventKey) {
        if (eventKey == null) return;
        for (MyEventEntry e : lotteryEntries) {
            if (eventKey.equals(e.getEventKey())) return; // already in list
        }
        lotteryEntries.add(new MyEventEntry(eventKey, EventStatus.PENDING, System.currentTimeMillis()));
    }

    /**
     * Call this when the user taps "Leave Waitlist". Removes the event from My Events.
     */
    public static void removeJoinedEvent(String eventKey) {
        if (eventKey == null) return;
        lotteryEntries.removeIf(e -> eventKey.equals(e.getEventKey()));
    }

    /**
     * Returns entries for the Lotteries tab (events user joined, not yet past registration).
     */
    public static List<MyEventEntry> getLotteryEntries() {
        return new ArrayList<>(lotteryEntries);
    }

    /**
     * Call this when the user declines an invite. Removes from Lotteries and adds to History with DECLINED.
     */
    public static void addDeclinedEvent(String eventKey) {
        if (eventKey == null) return;
        lotteryEntries.removeIf(e -> eventKey.equals(e.getEventKey()));
        for (MyEventEntry e : declinedEntries) {
            if (eventKey.equals(e.getEventKey())) return;
        }
        declinedEntries.add(new MyEventEntry(eventKey, EventStatus.DECLINED, System.currentTimeMillis()));
    }

    /**
     * Returns entries for the History tab (events the user declined after being selected).
     */
    public static List<MyEventEntry> getHistoryEntries() {
        return new ArrayList<>(declinedEntries);
    }
}
