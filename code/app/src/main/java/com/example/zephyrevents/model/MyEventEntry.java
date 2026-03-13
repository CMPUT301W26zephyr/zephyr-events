package com.example.zephyrevents.model;

import com.example.zephyrevents.util.SampleEvents;

/**
 * Represents one event in "My Events" (either in Lotteries or History).
 * Used to show event title, duration text ("1 day ago"), and status badge.
 */
public class MyEventEntry {

    /** Event key (e.g. {@link SampleEvents#KEY_SWIMMING}) so we can open the right event detail. */
    private final String eventKey;
    /** SELECTED or PENDING (shown as WAITING) for lotteries; null for history placeholders. */
    private final EventStatus status;
    /** When the user joined the waitlist */
    private final long joinedAtMillis;
    /** For history placeholder cards
    private final String placeholderTitle;

    /**
     * Creates an entry for a real event (Lotteries).
     */
    public MyEventEntry(String eventKey, EventStatus status, long joinedAtMillis) {
        this.eventKey = eventKey;
        this.status = status;
        this.joinedAtMillis = joinedAtMillis;
        this.placeholderTitle = null;
    }

    /**
     * Creates a placeholder entry for History
     */
    public MyEventEntry(String placeholderTitle) {
        this.eventKey = null;
        this.status = null;
        this.joinedAtMillis = 0;
        this.placeholderTitle = placeholderTitle;
    }

    public String getEventKey() {
        return eventKey;
    }

    public EventStatus getStatus() {
        return status;
    }

    public long getJoinedAtMillis() {
        return joinedAtMillis;
    }

    /** True if this is a History placeholder (not a real event). */
    public boolean isPlaceholder() {
        return placeholderTitle != null;
    }

    public String getPlaceholderTitle() {
        return placeholderTitle;
    }
}
