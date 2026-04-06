package com.example.zephyrevents.repository;

/**
 * Defines constant strings for firebase collection names.
 */
public final class Collections {

    // constants for firebase collections, avoids magic strings everywhere, prevents bugs.
    public static final String EVENTS = "events";

    public static final String USERS = "users";

    public static final String NOTIFICATIONS = "notifications";
    public static final String WAITLIST = "waitlist";

    /** Event discussion threads; documents include {@code eventId} for querying. */
    public static final String EVENT_COMMENTS = "event_comments";
    public static final String SYSTEM_LOGS = "system_logs";
}