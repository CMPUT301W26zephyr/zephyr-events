package com.example.zephyrevents.model;

/**
 * Represents the different types of notifications that can exist in our system.
 *
 * This is an enum. An enum in Java is a special type that lets us define a fixed
 * set of constants. In this case, these are the ONLY valid notification types.
 *
 * Why use an enum instead of strings?
 * - Prevents typos (e.g., "event_start" vs "EVENT_START")
 * - Ensures only valid values can be used
 * - Makes the code easier to read and maintain
 *
 * Example usage:
 *     NotificationType type = NotificationType.EVENT_START;
 *
 * You cannot create new values at runtime — only the ones listed here are allowed.
 */
public enum NotificationType {
    EVENT_START,
    EVENT_END,
    WON_EVENT,
    LOST_EVENT,
    MANUAL,
    /** Invited to join the waitlist of a private event. */
    PRIVATE_EVENT_INVITE,
    /** Invited to co-organize an event. */
    CO_ORGANIZER_INVITE
}
