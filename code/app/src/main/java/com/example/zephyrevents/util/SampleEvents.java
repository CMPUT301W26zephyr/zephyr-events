package com.example.zephyrevents.util;

import com.example.zephyrevents.model.Event;

import java.util.Calendar;

/**
 * Provides sample events for the event detail screens (Beginner Swimming Lessons and Piano Lessons).
 * Used so we can open the app and see the two events without loading from a database.
 */
public final class SampleEvents {

    /** Key to pass in Intent to open the swimming event. */
    public static final String KEY_SWIMMING = "swimming";
    /** Key to pass in Intent to open the piano event. */
    public static final String KEY_PIANO = "piano";
    /** Key to pass in Intent to open the yoga event. */
    public static final String KEY_YOGA = "yoga";

    private SampleEvents() {}

    /**
     * Returns the event for the given key, or null if the key is not recognized.
     */
    public static Event getEvent(String key) {
        if (KEY_SWIMMING.equals(key)) {
            return buildSwimmingEvent();
        }
        if (KEY_PIANO.equals(key)) {
            return buildPianoEvent();
        }
        if (KEY_YOGA.equals(key)) {
            return buildYogaEvent();
        }
        return null;
    }

    /**
     * Beginner Swimming Lessons: registration open, capacity 67, 20 applicants.
     */
    private static Event buildSwimmingEvent() {
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.FEBRUARY, 10, 23, 59, 59);
        long regEnd = cal.getTimeInMillis();
        return new Event(
                KEY_SWIMMING,
                "Beginner Swimming Lessons",
                "Learn essential skills like floating, kicking, and basic strokes with certified instructors in a safe, supportive, and fun environment.",
                System.currentTimeMillis(),
                System.currentTimeMillis() + 3600000,
                "$50.00",
                "Edmonton, Alberta",
                "John Doe",
                67,
                20,
                regEnd
        );
    }

    /**
     * Piano Lessons: registration closed full, capacity 15, 150 applicants.
     */
    private static Event buildPianoEvent() {
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.FEBRUARY, 15, 23, 59, 59);
        long regEnd = cal.getTimeInMillis();
        return new Event(
                KEY_PIANO,
                "Piano Lessons",
                "Learn piano at your own pace with fun, personalized lessons that build skills and confidence perfect for beginners and aspiring musicians alike.",
                System.currentTimeMillis(),
                System.currentTimeMillis() + 3600000,
                "$120.00",
                "Edmonton, Alberta",
                "Sally Brown",
                15,
                150,
                regEnd
        );
    }

    /**
     * Yoga Lessons: same format as swimming, $20. Used to demonstrate Accept/Decline invite flow (SELECTED).
     */
    private static Event buildYogaEvent() {
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.FEBRUARY, 10, 23, 59, 59);
        long regEnd = cal.getTimeInMillis();
        return new Event(
                KEY_YOGA,
                "Yoga Lessons",
                "Learn essential skills like breathing, stretching, and basic poses with certified instructors in a safe, supportive, and fun environment.",
                System.currentTimeMillis(),
                System.currentTimeMillis() + 3600000,
                "$20.00",
                "Edmonton, Alberta",
                "John Doe",
                67,
                20,
                regEnd
        );
    }
}
