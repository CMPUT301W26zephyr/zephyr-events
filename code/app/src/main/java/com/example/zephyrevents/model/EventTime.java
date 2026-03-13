package com.example.zephyrevents.model;

import com.example.zephyrevents.util.DistanceHelper;
import com.example.zephyrevents.util.TimeHelper;

/**
 * This is a class that encapsulates an Events's start and end time.
 */
public class EventTime {
    private  long startTime;
    private  long endTime;
    public EventTime(){}


    /**
     * Constructor, creates EventTime with start and end time.
     * @param startTime
     * @param endTime
     */
    public EventTime(long startTime, long endTime){
        this.startTime= startTime;
        this.endTime = endTime;
    }

    /**
     * Makes it super easy to get a formatted string of how much time is left for an event.
     * @return formatted string that shows how much time is left for an event.
     */
    public String eventTimeLeft(){
        var timeNow = TimeHelper.now();
        var timeLeft = TimeHelper.timeDifference(endTime, timeNow);
        return TimeHelper.formatDuration(TimeHelper.timeDifference(timeNow, timeLeft));
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
