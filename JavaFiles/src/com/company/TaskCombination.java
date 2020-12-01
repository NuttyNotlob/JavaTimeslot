package com.company;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TaskCombination {
    private Task[] tasks;
    private int totalDuration;

    public TaskCombination() {
        this.tasks = new Task[0];
        this.totalDuration = 0;
    }

    public TaskCombination(Task[] tasks) {
        this.tasks = tasks;
        this.totalDuration = calcTotalDuration(tasks);
    }

    public int calcTotalDuration (Task[] tasks) {
        // This function calculates the total duration of all the tasks in the combination
        int totalDuration = 0;

        for (int i = 0; i < tasks.length; i++) {
            totalDuration += tasks[i].getTimeTaken();
        }

        return totalDuration;
    }

    public Task[] getTasks() {
        return tasks;
    }

    public int getTotalDuration() {
        return totalDuration;
    }

    public void addTask(Task taskToAdd) {
        // First add the task to our list/array of tasks
        List<Task> tempTaskList = new ArrayList<>(Arrays.asList(tasks));
        tempTaskList.add(taskToAdd);
        this.tasks = tempTaskList.toArray(tasks);

        // And then add the duration to the total duration
        this.totalDuration += taskToAdd.getTimeTaken();
    }

    public String getTasksString() {
        // This function gives the list of all the tasks in this combination, in string form
        String taskString = "";

        // Loop through all the tasks and add their name to the string
        for (int i = 0; i < tasks.length; i++) {
            taskString += tasks[i].getTaskName() + " ";
        }

        return taskString;
    }
}
