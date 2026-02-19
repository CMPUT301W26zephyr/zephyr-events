package com.example.zephyrevents.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class TimeHelper {
    private TimeHelper() {}

    public static long now() {
        return System.currentTimeMillis();
    }

    public static String format(long millis, DateTimeFormat fmt, String timeZoneId){
        return format(millis, fmt.pattern(), TimeZone.getTimeZone(timeZoneId));
    }
    public static String format(long millis, String customPattern){
        return format(millis, customPattern, TimeZone.getDefault());
    }
    public static String format(long millis, DateTimeFormat fmt){
        return format(millis, fmt.pattern(), TimeZone.getDefault());
    }

    public static String format(long millis, String customPattern, TimeZone zone){
        Date date = new Date(millis);
        SimpleDateFormat sdf = new SimpleDateFormat(customPattern, Locale.CANADA);
        sdf.setTimeZone(zone);
        return sdf.format(date);

    }
    public static long timeDifference(long timeBeginning, long timeEnding){return timeBeginning-timeEnding;}




}
