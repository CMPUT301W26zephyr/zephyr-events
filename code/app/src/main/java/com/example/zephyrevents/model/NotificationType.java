package com.example.zephyrevents.model;

/**
 * Different types of notifications that can be sent.
 * Using an enum instead of strings ensures type safety and prevents invalid values.
 *
 * Going to update this later when I'm more sure of how our notification system will be setup.
 */
public enum NotificationType {
    EVENT_START,
    EVENT_END,
    WON_EVENT,
    LOST_EVENT
    // Add more later
}
