package com.example.zephyrevents.model;

import com.example.zephyrevents.util.DistanceHelper;
import com.example.zephyrevents.util.TimeHelper;

public class EventTime {
    private  long startTime;
    private  long endTime;
    public EventTime(){}



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
}
