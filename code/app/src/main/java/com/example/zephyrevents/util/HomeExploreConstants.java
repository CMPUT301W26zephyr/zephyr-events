package com.example.zephyrevents.util;

/**
 * Time windows for home explore rows and matching "See all" list filters.
 */
public final class HomeExploreConstants {
    private HomeExploreConstants() {}

    /** Registration end must fall within this many ms from now (urgency row). */
    public static final long CLOSING_SOON_MS = 48L * 60L * 60L * 1000L;

    /** Registration must have opened within this many ms of now (new listings). */
    public static final long NEW_ON_LOTTOFY_MAX_AGE_MS = 7L * 24L * 60L * 60L * 1000L;
}
