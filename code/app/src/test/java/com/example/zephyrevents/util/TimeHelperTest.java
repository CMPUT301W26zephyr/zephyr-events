package com.example.zephyrevents.util;

import static org.junit.Assert.*;
        import org.junit.Test;

// Anthropic, Claude, "Write unit tests for TimeHelper.java for an Android app", 2026-03-12

public class TimeHelperTest {

    // --- timeDifference ---

    @Test
    public void timeDifference_returnsCorrectDifference() {
        assertEquals(500L, TimeHelper.timeDifference(1000L, 1500L));
    }

    @Test
    public void timeDifference_returnsNegativeWhenStartAfterEnd() {
        assertEquals(-500L, TimeHelper.timeDifference(1500L, 1000L));
    }

    @Test
    public void timeDifference_returnsZeroWhenEqual() {
        assertEquals(0L, TimeHelper.timeDifference(1000L, 1000L));
    }

    // --- formatDuration ---

    @Test
    public void formatDuration_zeroMillis_returnsZeroSeconds() {
        assertEquals("0s", TimeHelper.formatDuration(0));
    }

    @Test
    public void formatDuration_negativeMillis_returnsZeroSeconds() {
        assertEquals("0s", TimeHelper.formatDuration(-5000));
    }

    @Test
    public void formatDuration_seconds_returnsSeconds() {
        assertEquals("30s", TimeHelper.formatDuration(30_000));
    }

    @Test
    public void formatDuration_minutes_returnsMinutesAndSeconds() {
        // 2 minutes 30 seconds
        assertEquals("2m 30s", TimeHelper.formatDuration(150_000));
    }

    @Test
    public void formatDuration_hours_returnsHoursAndMinutes() {
        // 1 hour 30 minutes
        assertEquals("1h 30m", TimeHelper.formatDuration(5_400_000));
    }

    @Test
    public void formatDuration_days_returnsDaysAndHours() {
        // 2 days 3 hours
        assertEquals("2d 3h", TimeHelper.formatDuration(183_600_000));
    }
}