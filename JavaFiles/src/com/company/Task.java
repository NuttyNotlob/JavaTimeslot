package com.company;

public class Task {
    private String taskName;
    private int timeTaken;
    private boolean assigned;

    public Task(String taskName, int timeTaken) {
        this(taskName, timeTaken, false);
    }

    public Task(String taskName, int timeTaken, boolean assigned) {
        this.taskName = taskName;
        this.timeTaken = timeTaken;
        this.assigned = assigned;
    }

    public String getTaskName() {
        return taskName;
    }

    public int getTimeTaken() {
        return timeTaken;
    }

    public boolean isAssigned() {
        return assigned;
    }
}
