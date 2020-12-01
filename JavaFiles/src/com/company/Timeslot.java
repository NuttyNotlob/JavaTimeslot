package com.company;

public class Timeslot {
    private Time startTime;
    private Time endTime;
    private boolean filled;
    private String timeslotTask;
    private int timeslotLength;

    public Timeslot() {
        this(new Time(0, 0), new Time(0, 0));
    }

    public Timeslot(Time startTime, Time endTime) {
        this(startTime, endTime, false, "");
    }

    public Timeslot(Time startTime, Time endTime, boolean filled, String timeslotTask) {
        if (endTime.getHours() < startTime.getHours()) {
            System.out.println("Invalid timeslot - end time must be after start time");
        } else if ((endTime.getHours() == startTime.getHours()) && (endTime.getMinutes() < startTime.getMinutes())) {
            System.out.println("Invalid timeslot - end time must be after start time");
        } else {
            this.startTime = startTime;
            this.endTime = endTime;
        }
        this.filled = filled;
        this.timeslotTask = timeslotTask;
        this.timeslotLength = startTime.calculateTimeDifference(endTime);
    }

    public int getTimeslotLength() {
        return timeslotLength;
    }

    public Time getStartTime() {
        return startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public boolean isFilled() {
        return filled;
    }

    public String getTimeslotTask() {
        return timeslotTask;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public void setFilled(boolean filled) {
        this.filled = filled;
    }

    public void setTimeslotTask(String timeslotTask) {
        this.timeslotTask = timeslotTask;
    }
}
