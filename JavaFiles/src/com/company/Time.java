package com.company;

public class Time {
    private int hours;
    private int minutes;

    // Potential confusion in that this is a particular time (e.g. 15.15), not a length of time.

    public Time(int hour, int minutes) {
        // Checks made in constructor so that only a valid time can be set

        if (hour >= 0 && hour <= 23) {
            this.hours = hour;
        } else {
            System.out.println("Invalid time format");
        }
        if (minutes >= 0 && minutes <= 59) {
            this.minutes = minutes;
        } else {
            System.out.println("Invalid time format");
        }
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public void addTime(int hours, int minutes) {
        // Keep count of original time so if we go past valid amount, we can reset to it
        int originalHour = this.hours;
        int originalMinute = this.minutes;

        // Add the minutes on, adding to hours if we go past the 60 mark and then using modulus to find relevant
        // minute value
        this.minutes += minutes;

        if (this.minutes >= 60) {
            this.hours += this.minutes / 60;
            this.minutes %= 60;
        }

        // Add the hours on. If we go past 23, we say this is invalid as system will only handle specific days for now.
        // Add the hours second so that this check is made after fiddling with the minutes
        if (this.hours + hours <=23) {
            this.hours += hours;
        } else {
            System.out.println("Invalid time addition - system cannot go past one day. Time reset to original");
            this.hours = originalHour;
            this.minutes = originalMinute;
        }
    }

    public int calculateTimeDifference (Time targetTime) {
        // Set initial variable
        int timeDifference = 0;

        // Add the hours (x60) and minutes onto the timeDifference variable, and then return
        timeDifference += (targetTime.getHours() - this.getHours()) * 60;
        timeDifference += (targetTime.getMinutes() - this.getMinutes());

        return timeDifference;
    }

    public String getTimeString() {
        if (minutes < 10) {
            if (hours < 10) {
                return "0" + hours + ":0" + minutes;
            } else {
                return hours + ":0" + minutes;
            }
        } else {
            if (hours < 10) {
                return "0" + hours + ":" + minutes;
            } else {
                return hours + ":" + minutes;
            }
        }
    }

    public double getTimeDouble() {
        return hours + ((double)minutes / 100);
    }
}
