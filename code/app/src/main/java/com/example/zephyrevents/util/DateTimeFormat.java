package com.example.zephyrevents.util;

public enum DateTimeFormat {
    MONTH_DAY_YEAR("MM/dd/yyyy"), // 02/18/2026
    DAY_MONTH_YEAR("dd-M-yyyy"), // 18/02/2026
    WEEK_MONTH_DAY_YEAR("EEE, MMM d, ''yy"), // Wed, Feb 18, `26
    TIME("h:mm a"), // 10:37 PM
    ARMY_TIME("HH:mm:ss"); //22:38:27

    private final String pattern;
    DateTimeFormat(String pattern){
        this.pattern = pattern;
    }
    public String pattern() {
        return pattern;
    }
}
