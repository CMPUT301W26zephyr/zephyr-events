package com.example.zephyrevents.model;

/**
 * Represents the different status types an event can take in our system.
 *
 * This is an enum. An enum in Java is a special type that lets us define a fixed
 * set of constants. In this case, these are the ONLY valid EventStatus types.
 *
 * Why use an enum instead of strings?
 * - Prevents typos (e.g., "Accepted" vs "ACCEPTED")
 * - Ensures only valid values can be used
 * - Makes the code easier to read and maintain
 *
 * Example usage:
 *     EventStatus status = EventStatus.SELECTED;
 *
 * You cannot create new values at runtime — only the ones listed here are allowed.
 */
public enum EventStatus {
    PENDING,
    OPEN,
    FULL,
    CLOSED,
    CANCELLED,
    COMPLETED
}
