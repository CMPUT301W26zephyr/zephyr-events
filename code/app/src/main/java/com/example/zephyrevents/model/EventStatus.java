package com.example.zephyrevents.model;

/**
 * Status values for a waitlist entry.
 * Using an enum instead of strings ensures type safety and prevents invalid values.
 */
public enum EventStatus {
    ACCEPTED,
    DECLINED,
    CANCELLED,
    SELECTED,
    PENDING
}
