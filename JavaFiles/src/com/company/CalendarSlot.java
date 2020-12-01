package com.company;

public class CalendarSlot {
    private String slotName;
    private Time startTime;
    private Time endTime;

    public CalendarSlot(String slotName, Time startTime, Time endTime) {
        this.slotName = slotName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getSlotName() {
        return slotName;
    }

    public Time getStartTime() {
        return startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setSlotName(String slotName) {
        this.slotName = slotName;
    }
}
