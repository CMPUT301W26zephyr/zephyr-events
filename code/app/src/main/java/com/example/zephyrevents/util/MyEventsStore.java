package com.example.zephyrevents.util;

import com.example.zephyrevents.model.EventStatus;
import com.example.zephyrevents.model.MyEventEntry;
import com.example.zephyrevents.model.Status;
import com.example.zephyrevents.model.User;
import com.example.zephyrevents.model.WaitlistEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory store of events the user has joined (Join Waitlist).
 * When the user taps "Join Waitlist" on an event, that event is added here
 * so it appears on the My Events → Lotteries tab.
 */
public final class MyEventsStore {

    private static final List<WaitlistEntry> lotteryEntries = new CopyOnWriteArrayList<>();
    private static final List<WaitlistEntry> declinedEntries = new CopyOnWriteArrayList<>();

    static {
        long oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000L);

        WaitlistEntry yogaEntry = new WaitlistEntry(
                "demo-user",
                SampleEvents.KEY_YOGA,
                53.5461,
                -113.4938,
                Status.SELECTED
        );
        yogaEntry.setTimestamp(oneDayAgo);

        WaitlistEntry swimmingEntry = new WaitlistEntry(
                "demo-user",
                SampleEvents.KEY_SWIMMING,
                53.5461,
                -113.4938,
                Status.WAITLISTED
        );

        lotteryEntries.add(yogaEntry);
        lotteryEntries.add(swimmingEntry);

    }
    private MyEventsStore() {}

    /**
     * Returns true if the user has joined the waitlist for this event (and not left it).
     */
    public static boolean isOnWaitlist(String eventKey) {
        if (eventKey == null) return false;
        for (WaitlistEntry e : lotteryEntries) {
            if (eventKey.equals(e.getEventId())) return true;
        }
        return false;
    }

    /**
     * Call this when the user taps "Join Waitlist" on an event.
     * Adds the event to the lotteries list with status WAITING (PENDING) if not already present.
     */
    public static void addJoinedEvent(String eventKey, User user) {
        if (eventKey == null) return;
        for (WaitlistEntry e : lotteryEntries) {
            if (eventKey.equals(e.getEventId())) return; // already in list
        }
        lotteryEntries.add(new WaitlistEntry(user.getId(), eventKey, 53.5461, -113.4938,Status.WAITLISTED));
    }

    /**
     * Call this when the user taps "Leave Waitlist". Removes the event from My Events.
     */
    public static void removeJoinedEvent(String eventKey) {
        if (eventKey == null) return;
        lotteryEntries.removeIf(e -> eventKey.equals(e.getEventId()));
    }

    /**
     * Returns entries for the Lotteries tab (events user joined, not yet past registration).
     */
    public static List<WaitlistEntry> getLotteryEntries() {
        return new ArrayList<>(lotteryEntries);
    }

    /**
     * Call this when the user declines an invite. Removes from Lotteries and adds to History with DECLINED.
     */
    public static void addDeclinedEvent(String eventKey, User user) {
        if (eventKey == null) return;
        lotteryEntries.removeIf(e -> eventKey.equals(e.getEventId()));
        for (WaitlistEntry e : declinedEntries) {
            if (eventKey.equals(e.getEventId())) return;
        }
        declinedEntries.add(new WaitlistEntry(user.getId(), eventKey, 53.5461, -113.4938, Status.DECLINED));
    }

    /**
     * Returns entries for the History tab (events the user declined after being selected).
     */
    public static List<WaitlistEntry> getHistoryEntries() {
        return new ArrayList<WaitlistEntry>(declinedEntries);
    }
}
