package com.example.zephyrevents.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A class that helps with time related utilities with the goal to make working with milliseconds easier.
 */
public final class TimeHelper {
    private static final long MINUTE = 60;
    private static final long HOUR = 60 * MINUTE;
    private static final long DAY = 24 * HOUR;
    private TimeHelper() {}

    /**
     * This method is a simple abstraction of System.currentTimeMillis, but will allow the team
     * to have a consistent way to handle time.
     * @return returns a long representing current time in milliseconds
     */
    public static long now() {
        return System.currentTimeMillis();
    }

    /**
     * This method formats milliseconds to a specific DateTimeFormat that the user passes
     * while accounting for timezone.
     * @param millis Time in milliseconds.
     * @param fmt A selected
     * @param timeZoneId
     * @return
     * <pre>
     *     // Care for a similar package named the same thing. Use the enum one! with the purple E.
     *     DateTimeFormat fmt = DateTimeFormat.MONTH_DAY_YEAR;
     *     // Notice how I use the . to access the which format I want.
     *     // Check DateTimeFormat.java for more.
     *
     *      String timeZoneId = "America/Edmonton";
     *     // See how the enum is passed and is automatically turned into this format MM/dd/yyyy
     *     var String formattedDate= TimeHelper.Format(millis, fmt, timeZoneId);
     *     // now you got a clean date in the format of 02/18/2026 :) yay!
     *     return formattedDate;
     * </pre>
     *
     */
    public static String format(long millis, DateTimeFormat fmt, String timeZoneId){
        return format(millis, fmt.pattern(), TimeZone.getTimeZone(timeZoneId));
    }

    /**
     * A derivative of the previous format methods but with different input params.
     * Default timezone is used.
     * @param millis Time in milliseconds
     * @param customPattern A string that represents a custom time format.
     * @return Formatted string with the time in the chosen format.
     */
    public static String format(long millis, String customPattern){
        return format(millis, customPattern, TimeZone.getDefault());
    }

    /**
     * A derivative of the previous format methods but with different input params.
     * Default timezone is used.
     * @param millis Time in milliseconds
     * @param fmt A selected fmt type based on the DateTimeFormat enum, see usage example below.
     * @return Formatted string with the time in the chosen format.
     * <pre>
     *      // Care for a similar package named the same thing. Use the enum one! with the purple E.
     *      *     DateTimeFormat fmt = DateTimeFormat.MONTH_DAY_YEAR;
     *      *     // Notice how I use the . to access the which format I want.
     *      *     // Check DateTimeFormat.java for more.
     *      *
     *      *     // See how the enum is passed and is automatically turned into this format MM/dd/yyyy
     *      *     var String formattedDate= TimeHelper.Format(millis, fmt);
     *      *     // now you got a clean date in the format of 02/18/2026 :) yay!
     *      *     return formattedDate;
     * </pre>
     */
    public static String format(long millis, DateTimeFormat fmt){
        return format(millis, fmt.pattern(), TimeZone.getDefault());
    }

    /**
     * Similar to the other format method, but instead of using an enum, user can pass their own
     * custom format strings.
     * @param millis Time in milliseconds
     * @param customPattern A string that represents a custom time format if you need
     * @param zone  A timezone object of the TimeZone package
     * @return Formatted string with the time in the chosen format.
     */
    public static String format(long millis, String customPattern, TimeZone zone){
        Date date = new Date(millis);
        SimpleDateFormat sdf = new SimpleDateFormat(customPattern, Locale.CANADA);
        sdf.setTimeZone(zone);
        return sdf.format(date);
    }

    /**
     * Method for quickly checking the difference between two millisecond times.
     * @param startTime Start time in milliseconds
     * @param endTime end time in milliseconds
     * @return the difference in times in milliseconds.
     */
    public static long timeDifference(long startTime, long endTime){return endTime-startTime;}
    public static String formatDuration(long millis) {
        if (millis <= 0) {
            return "0s";
        }

        long totalSeconds = millis / 1000;

        long days = totalSeconds / DAY;
        long hours = (totalSeconds % DAY) / HOUR;
        long minutes = (totalSeconds % HOUR) / MINUTE;
        long seconds = totalSeconds % MINUTE;

        if (days > 0) {
            return days + "d " + hours + "h";
        }

        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }

        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }

        return seconds + "s";
    }
}

