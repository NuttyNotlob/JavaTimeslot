package com.company;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // Set the variables needed for initial timeslot creation - start and end times, and the interval to be
        // used for timeslots (may change these to be user input later)
        // todo potentially set these as user inputs
        int timeSlotInterval = 15;
        Time dayStartTime = new Time(9, 0);
        Time dayEndTime = new Time(17, 0);

        // Create the array of timeslots for our day
        Timeslot[] daySchedule = createTimeslots(dayStartTime, dayEndTime, timeSlotInterval);

        // Now get the calendar slots already filled by other tasks, via user input
        CalendarSlot[] filledCalendarSlots = getFullSlots();

        // Go through the timeslots and fill these up
        fillTimeslots(daySchedule, filledCalendarSlots);

        // Find the empty slots in our schedule that we can put tasks into
        Timeslot[] freeSlots = findGaps(daySchedule);

        // todo ask user how they would like the tasks to be allocated - earliest in the day, or best fitting (optimised)
        //  timeslot

        // Then we sort these timeslots to better optimise our greedy algorithm
        freeSlots = sortTimeslots(freeSlots);

        // Print out the timeslots it finds that are free for us to put tasks into
        System.out.println("System found " + freeSlots.length + " free slots: ");

        for (int i = 0; i < freeSlots.length; i++) {
            System.out.println(freeSlots[i].getStartTime().getTimeString() + " - " +
                    freeSlots[i].getEndTime().getTimeString() + " : " + freeSlots[i].getTimeslotLength() + " minutes");
        }

        // Now we get the task list from user input on what they want to fit into their schedule
        Task[] taskList = getTaskList();

        // We then get the combinations of all of these tasks (slicing it to remove the empty base case we get from
        // the combinations method)
        TaskCombination[] taskCombinations = findTaskCombinations(taskList);
        taskCombinations = sliceTaskCombinationArray(taskCombinations, 1, taskCombinations.length - 1);

        // Go through the free timeslots we have, and assign task combinations to these. We then also remove the tasks
        // used for this from our combination list for our next timeslot check

        for (int i = 0; i < freeSlots.length; i++) {
            // Find the best task combination
            TaskCombination timeslotOptimalTasks = findBestTasks(taskCombinations, freeSlots[i].getTimeslotLength());

            // Fill this into the daySchedule
            fillTasks(timeslotOptimalTasks, freeSlots[i], daySchedule);

            // Remove these combinations from the overall list
            taskCombinations = removeCombinations(taskCombinations, timeslotOptimalTasks);
        }

        // We then clean up our schedule before printing it
        daySchedule = cleanSchedule(daySchedule);

        // Print out our schedule of timeslots, with their associated calendar slots already filled
        System.out.println("\n \n \n \nYour day's schedule, with tasks, is as follows: \n");
        for (int i = 0; i < daySchedule.length; i++) {
            System.out.println(daySchedule[i].getStartTime().getTimeString() + " - " +
                    daySchedule[i].getEndTime().getTimeString() + " : " + daySchedule[i].getTimeslotTask());
        }

        // Now, we find out any tasks that we weren't able to fit into the day's schedule
        TaskCombination leftoverTasks = getRemainingTasks(taskCombinations);

        // And if there are any leftover tasks, we print them out
        if (leftoverTasks.getTasks().length != 0) {
            System.out.println("\nThe following tasks could not be fit into the schedule: \n");
            for (int i = 0; i < leftoverTasks.getTasks().length; i++) {
                System.out.println(leftoverTasks.getTasks()[i].getTaskName());
            }

        }

    }

    public static Timeslot[] createTimeslots(Time dayStart, Time dayEnd, int timeslotLengthMinutes) {
        // Set out initial variables
        Time tempTimeslotStart = dayStart;
        int arrayCount = 0;

        // Find length of array needed
        int timeslotsRequired = (dayStart.calculateTimeDifference(dayEnd) / timeslotLengthMinutes) + 1;
        Timeslot[] timeslotArray = new Timeslot[timeslotsRequired - 1];

        // Convert timeslotLengthMinutes to hours and minutes so we can use it in addTime() Time class method
        int timeslotHours = timeslotLengthMinutes / 60;
        int timeslotMinutes = timeslotLengthMinutes % 60;

        // Now we iterate from the dayStart to dayEnd, in gaps of the timeslot interval specified
        while (tempTimeslotStart.calculateTimeDifference(dayEnd) > 0) {
            // Find the timeslot end for this timeslot
            Time tempTimeslotEnd = new Time(tempTimeslotStart.getHours(), tempTimeslotStart.getMinutes());
            tempTimeslotEnd.addTime(timeslotHours, timeslotMinutes);

            // Add the timeslot, using the tempTimeslotStart and timeslotEnd we've calculated. We start them all as
            // unfilled
            timeslotArray[arrayCount] = new Timeslot(tempTimeslotStart, tempTimeslotEnd);

            // Now set it up ready for the next iteration, by moving along the time and the array count
            tempTimeslotStart = tempTimeslotEnd;
            arrayCount++;
        }

        // Finally, return the array of timeslots we have
        return timeslotArray;
    }

    public static CalendarSlot[] getFullSlots() {
        // Set up initial array and list. We use a list so that we can keep adding slots while the user keeps inputting
        // calendar slots
        CalendarSlot[] calendarSlotArray = new CalendarSlot[0];
        List<CalendarSlot> calendarSlotList = new ArrayList<>(Arrays.asList(calendarSlotArray));

        // Set up scanner. This will read our user input
        Scanner scanner = new Scanner(System.in);

        while (true) {
            // Initialise start and end time hour & min values
            int calendarSlotStartHour, calendarSlotStartMinute;
            int calendarSlotEndHour, calendarSlotEndMinute;

            // First we get the calendar slot name
            System.out.println("Enter the name of your calendar slot already taken. Enter 'Done' to stop adding calendar slots");
            String calendarSlotString = scanner.nextLine();

            // Validate if the user wants to stop
            if (calendarSlotString.toLowerCase().equals("done")) {
                break;
            }

            // Now we get the start time
            System.out.println("Enter the start time of this calendar slot, in the format 0.00 (e.g. 1.15pm = 13.15) \n" +
                    "NOTE: this should be in 24-hr format");
            double calendarStartDouble = scanner.nextDouble();
            scanner.nextLine();

            // Validate if the time entered is valid
            // todo need to validate there are maximum of two decimal places here
            // todo limit validation to be within the dayStart and dayEnd specified
            // todo need a boolean to check if the next input is a double before doing this, otherwise we get errors
            if (calendarStartDouble > 23.59 || calendarStartDouble < 0 || calendarStartDouble % 1 >= 0.6) {
                System.out.println("Invalid time value given");
                continue;
            } else {
                calendarSlotStartHour = (int)calendarStartDouble;
                calendarSlotStartMinute = (int)((calendarStartDouble % 1) * 100);
            }

            // Now we get the end time
            System.out.println("Enter the end time of this calendar slot, in the format 0.00 (e.g. 1.15pm = 13.15) \n" +
                    "NOTE: this should be in 24-hr format");
            double calendarEndDouble = scanner.nextDouble();
            scanner.nextLine();

            // Validate if the time entered is valid
            if (calendarEndDouble > 23.59 || calendarEndDouble < 0 || calendarEndDouble % 1 >= 0.6) {
                System.out.println("Invalid time value given");
                continue;
            } else {
                calendarSlotEndHour = (int)calendarEndDouble;
                calendarSlotEndMinute = (int)((calendarEndDouble % 1) * 100);
            }

            // Take the input values and add them to our calendar slot list/array
            calendarSlotList.add(new CalendarSlot(calendarSlotString, new Time(calendarSlotStartHour, calendarSlotStartMinute),
                    new Time(calendarSlotEndHour, calendarSlotEndMinute)));
        }

        // Convert our list back into an array and then return it
        calendarSlotArray = calendarSlotList.toArray(calendarSlotArray);

        return calendarSlotArray;
    }

    public static void fillTimeslots(Timeslot[] timeslotArray, CalendarSlot[] calendarSlotArray) {
        // This  function iterates through each CalendarSlot, and then subsequently through each Timeslot to see if the
        // timeslot is filled by the task

        // To compare our two time doubles together, we have a threshold of one minute (without this, we get bugs
        // in our comparison)
        double compareThreshold = 0.01;

        for (int i = 0; i < calendarSlotArray.length; i++) {
            for (int j = 0; j < timeslotArray.length; j++) {
                if (((timeslotArray[j].getStartTime().getTimeDouble() + compareThreshold >= calendarSlotArray[i].getStartTime().getTimeDouble()) &&
                        (timeslotArray[j].getEndTime().getTimeDouble() - compareThreshold <= calendarSlotArray[i].getEndTime().getTimeDouble()))) {

                    // If they overlap, we mark the Task as filled and set its taskName to be the name of our
                    // CalendarSlot
                    timeslotArray[j].setFilled(true);
                    timeslotArray[j].setTimeslotTask(calendarSlotArray[i].getSlotName());
                }
            }
        }
    }

    public static Timeslot[] findGaps(Timeslot[] timeslotArray) {
        // Code iterates through the timeslot array given, and outputs a timeslot array with the times where the
        // user is free

        // First make an array and then convert it to a list so we can add to it as we go
        Timeslot[] freeTimeslotsArray = new Timeslot[0];
        List<Timeslot> freeTimeslotsList = new ArrayList<>(Arrays.asList(freeTimeslotsArray));

        // Then set out variables to be used in loop
        Time timeslotStart = new Time(0, 0);
        Time timeslotEnd = new Time(0, 0);
        Timeslot tempTimeslotStore = new Timeslot(timeslotStart, timeslotEnd);

        for (int i = 0; i < timeslotArray.length; i++) {
            if (!timeslotArray[i].isFilled()) {
                // If the timeslot isn't filled, we follow this path, as we're either making a new timeslot to be added
                // to our output list, or extending one that's already going

                if (tempTimeslotStore.getTimeslotLength() == 0) {
                    // If the tempTimeslot length is 0, that means we're starting a new one, so we set the start
                    // time to be the current timeslot's (in the array) start
                    timeslotStart = timeslotArray[i].getStartTime();

                }

                // Either way, we set the end time to be the current timeslot's (from the array) end time, either to
                // start it off or extend it
                timeslotEnd = timeslotArray[i].getEndTime();
                tempTimeslotStore = new Timeslot(timeslotStart, timeslotEnd);

            } else {
                // If it isn't filled, we either need to end the timeslot we were previously making, or just continue
                // until we find one that isn't filled

                if (tempTimeslotStore.getTimeslotLength() != 0) {
                    // If the timeslot length isn't 0, we have a timeslot to store to our list
                    freeTimeslotsList.add(tempTimeslotStore);

                    // We then reset our variables. ready for the next one
                    timeslotStart = new Time(0, 0);
                    timeslotEnd = new Time(0, 0);
                    tempTimeslotStore = new Timeslot(timeslotStart, timeslotEnd);
                }
                // Otherwise, if the timeslot length is equal to 0, then we don't worry and we just want to continue
            }
        }

        // Now need one last check in case we got to the end with an open timeslot
        if (tempTimeslotStore.getTimeslotLength() != 0) {
            // If the timeslot length isn't 0, we have a timeslot to store to our list
            freeTimeslotsList.add(tempTimeslotStore);
        }

        // And finally, convert our list back into an array and then return it from the function
        freeTimeslotsArray = freeTimeslotsList.toArray(freeTimeslotsArray);
        return freeTimeslotsArray;
    }

    public static Task[] getTaskList() {
        // This function is designed to take the tasks that the user wants added into their schedule

        // First we set up the scanner
        Scanner scanner = new Scanner(System.in);

        // Next we set up the required array and list. We use a list so that we can add to it without knowing its length
        Task[] taskArrayOutput = new Task[0];
        List<Task> taskList = new ArrayList<>(Arrays.asList(taskArrayOutput));

        while (true) {
            // First we get the name of the task
            System.out.println("Enter the name of your task. Enter 'Done' to stop adding tasks");
            String taskString = scanner.nextLine();

            // Validate if the user wants to stop
            if (taskString.toLowerCase().equals("done")) {
                break;
            }

            // Now we get the duration of the task
            System.out.println("How long will this task take (minutes)?");
            int taskDuration = scanner.nextInt();
            scanner.nextLine();

            // Validate task duration given was valid
            if (taskDuration < 0) {
                System.out.println("Invalid task duration");
                continue;
            }

            // Now we add this to our task list
            taskList.add(new Task(taskString, taskDuration));
        }

        // Finally, we convert our list back into an array and return it as the function output
        taskArrayOutput = taskList.toArray(taskArrayOutput);
        return taskArrayOutput;
    }

    public static TaskCombination[] findTaskCombinations(Task[] tasks) {
        // This is a recursive function, so just a tad complicated

        // Base case
        if (tasks.length == 0) {
            TaskCombination[] taskCombinations = new TaskCombination[1];
            taskCombinations[0] = new TaskCombination();
            return taskCombinations;
        }

        // For remainder we take off one task from the first index
        Task firstTask = tasks[0];
        Task[] remainingTasks = sliceTaskArray(tasks, 1, tasks.length - 1);

        // Then we return our overall combinations. This is first made up of a copy of all the combinations of the
        // remainingTasks, and then this same copy with our fist index task added onto it
        TaskCombination[] taskCombinations = findTaskCombinations(remainingTasks);

        // To get the copy, we need to add to our array so we convert to a list
        List<TaskCombination> taskCombinationsList = new ArrayList<>(Arrays.asList(taskCombinations));

        // Then we iterate through our array, and add our firstTask to each combination. Then, we add this new
        // combination onto our list
        for (int i = 0; i < taskCombinations.length; i++) {
            // Potential this fails, don't know enough about memory to know for sure so will have to test
            TaskCombination tempTaskCombination = new TaskCombination(taskCombinations[i].getTasks());
            tempTaskCombination.addTask(firstTask);

            taskCombinationsList.add(tempTaskCombination);
        }

        // Finally, we convert back into an array and return this
        taskCombinations = taskCombinationsList.toArray(taskCombinations);

        return taskCombinations;

    }

    private static Task[] sliceTaskArray(Task[] tasks, int start, int end) {
        // This function essentially 'slices' an array, by making a copy of all the elements you want to slice
        Task[] slicedArray = new Task[end - start + 1];

        for (int i = start; i <= end; i ++) {
            slicedArray[i - start] = tasks[i];
        }

        return slicedArray;
    }

    private static TaskCombination[] sliceTaskCombinationArray(TaskCombination[] taskCombinations, int start, int end) {
        // This function essentially 'slices' an array, by making a copy of all the elements you want to slice
        TaskCombination[] slicedArray = new TaskCombination[end - start + 1];

        for (int i = start; i <= end; i ++) {
            slicedArray[i - start] = taskCombinations[i];
        }

        return slicedArray;
    }

    public static TaskCombination findBestTasks(TaskCombination[] taskCombinationArray, int timeslotLength) {
        // This function aims to find the best TaskCombination to fit a given timeslot length

        // First, we set out the required variables for finding the optimal combination
        TaskCombination optimalCombination = new TaskCombination();
        int optimalCombinationDuration = 0;

        for (int i = 0; i < taskCombinationArray.length; i++) {
            // We loop through our different combinations, and check if the duration is less than our timeslot length,
            // but also better than our previous optimal combination
            if (taskCombinationArray[i].getTotalDuration() <= timeslotLength &&
                    taskCombinationArray[i].getTotalDuration() > optimalCombinationDuration) {
                // If it is, we set our new optimal combination to be this set
                optimalCombination = new TaskCombination(taskCombinationArray[i].getTasks());
                optimalCombinationDuration = taskCombinationArray[i].getTotalDuration();
            }
        }

        // The best combination is then returned. If it doesn't find any combination less than the timeslot length,
        // then it will return an empty combination
        // todo check if this causes a problem through providing nulls

        return optimalCombination;
    }
    
    public static TaskCombination[] removeCombinations(TaskCombination[] taskCombinationArray, TaskCombination tasksToRemove) {
        // This function works through each task in the task combination given, and removes any combinations in the
        // given array that contain that task
        TaskCombination[] newCombinationArray = new TaskCombination[0];
        newCombinationArray = taskCombinationArray;

        // todo this function will not work correctly if multiple tasks have the same name

        for (int i = 0; i < tasksToRemove.getTasks().length; i++) {
            // Set our removal string to be the name of the task in our combination
            String taskRemovalString = tasksToRemove.getTasks()[i].getTaskName();

            // Loop through all our combinations
            for (int j = 0; j < taskCombinationArray.length; j++) {

                // If our combination's string contains the name of the task to be removed, we remove it from our array
                if (taskCombinationArray[j].getTasksString().contains(taskRemovalString)) {
                    newCombinationArray = removeCombinationElement(newCombinationArray, taskCombinationArray[j]);
                }
            }
        }

        return newCombinationArray;
    }

    public static TaskCombination[] removeCombinationElement(TaskCombination[] taskCombinationArray, TaskCombination combinationToRemove) {
        // In this function, we're basically going to make a copy of our array, without one of the elements
        TaskCombination[] newCombinationArray = new TaskCombination[0];
        List<TaskCombination> newCombinationList = new ArrayList<>(Arrays.asList(newCombinationArray));

        // We loop through our array, and if the TaskCombination isn't the same as the one we want to remove (found
        // by comparing the Task strings), we add it to our list, thereby having a new list with everything except
        // the one we want removed
        for (int i = 0; i < taskCombinationArray.length; i++) {
            if (!taskCombinationArray[i].getTasksString().equals(combinationToRemove.getTasksString())) {
                newCombinationList.add(taskCombinationArray[i]);
            }
        }

        // We then convert our list back into an array and return it
        newCombinationArray = newCombinationList.toArray(newCombinationArray);
        return newCombinationArray;
    }

    public static void fillTasks(TaskCombination taskCombination, Timeslot timeGap, Timeslot[] daySchedule) {
        // This function will go through the daySchedule, find where the gap is in there, and then assign the tasks
        // from the taskCombination to it

        // Set our variables required for this function
        int scheduleElementNumber = 0;

        // First we find where the gap is in our daySchedule

        for (int i = 0; i < daySchedule.length; i++) {
            if (daySchedule[i].getStartTime() == timeGap.getStartTime()) {
                // When we hit this if function, we've found our timeGap start
                scheduleElementNumber = i;
                break;
            }
        }

        // Now that we know where the gap starts, we want to start assigning our tasks
        for (int i = 0; i < taskCombination.getTasks().length; i++) {
            int taskTimeRemaining = taskCombination.getTasks()[i].getTimeTaken();

            while (taskTimeRemaining > 0) {
                // When we reach this part, we fill the timeslot in the schedule with the associated task
                daySchedule[scheduleElementNumber].setFilled(true);
                daySchedule[scheduleElementNumber].setTimeslotTask(taskCombination.getTasks()[i].getTaskName());

                // We then reduce the remaining time of that task, and move onto the next daySchedule timeslot
                taskTimeRemaining -= daySchedule[scheduleElementNumber].getTimeslotLength();
                scheduleElementNumber ++;
            }
        }
    }

    public static Timeslot[] cleanSchedule(Timeslot[] daySchedule) {
        // This function tidies up the schedule so it doesn't repeat, and also shortens it
        // First, we make the new schedule we will return
        Timeslot[] cleanedSchedule = new Timeslot[0];
        List<Timeslot> cleanedScheduleList = new ArrayList<>(Arrays.asList(cleanedSchedule.clone()));

        // Now we set out our variables that will be used in our schedule loop
        String currentTaskCheck = "";
        Time slotStartTime = new Time(0, 0);
        Time slotEndTime = new Time(0, 0);

        for (int i = 0; i < daySchedule.length; i++) {
            // We check if this is a new timeslot by comparing against our current string check
            if (!daySchedule[i].getTimeslotTask().equals(currentTaskCheck)) {

                // If it is new, then we cut off our previous one and add it to our list (as long as it doesn't have a
                // length of 0, so we don't get an empty one at the start
                if (slotStartTime.calculateTimeDifference(slotEndTime) != 0) {
                    cleanedScheduleList.add(new Timeslot(slotStartTime, slotEndTime, true, currentTaskCheck));
                }

                // We then set the new start time, as well as the string of that task
                slotStartTime = new Time(daySchedule[i].getStartTime().getHours(), daySchedule[i].getStartTime().getMinutes());
                currentTaskCheck = daySchedule[i].getTimeslotTask();
            }

            // Regardless of whether it's a new one or not, we extend the end time
            slotEndTime = new Time(daySchedule[i].getEndTime().getHours(), daySchedule[i].getEndTime().getMinutes());

        }

        // We then need to add one more element to catch the last part of the schedule
        cleanedScheduleList.add(new Timeslot(slotStartTime, slotEndTime, true, currentTaskCheck));


        // We then turn our list back into an array, and return it from the function
        cleanedSchedule = cleanedScheduleList.toArray(cleanedSchedule);
        return cleanedSchedule;

    }

    public static Timeslot[] sortTimeslots(Timeslot[] timeslotArray) {
        // To sort our combinations, we will be using a bubble sort. We will be implementing a check to see if the array
        // is already sorted, to increase the speed of our sort

        // First we set out our temporary variables used for the sorting
        Timeslot[] sortedArray = Arrays.copyOf(timeslotArray, timeslotArray.length);
        Timeslot tempTimeslot = new Timeslot();

        // Now we loop through our array elements n - 1 times, and sort each pair of elements bit by bit
        for (int i = 0; i < sortedArray.length - 1; i++) {
            int swapsMade = 0;
            for (int j = 0; j < sortedArray.length - 1; j++) {
                // We have two outcomes here - they are already in the right place, or they need to be switched
                if (sortedArray[j].getTimeslotLength() < sortedArray[j + 1].getTimeslotLength()) {
                    // Here, it's in the right place, so we continue
                    continue;
                } else {
                    // Here, the two need to be switched
                    tempTimeslot = sortedArray[j+1];
                    sortedArray[j+1] = sortedArray[j];
                    sortedArray[j] = tempTimeslot;

                    swapsMade++;
                }
            }

            // If we didn't make any swaps this pass, then we know that we can stop
            if (swapsMade == 0) {
                break;
            }
        }

        // Finally, we return our sorted array
        return sortedArray;
    }

    public static TaskCombination getRemainingTasks(TaskCombination[] leftoverCombinations) {
        // This function is aimed at finding the leftover tasks that weren't assigned to a timeslot

        // We'll be making a TaskCombination from all our remaining tasks
        TaskCombination leftoverTasks = new TaskCombination();

        // If we don't have any leftover combinations then we just return an empty taskCombination
        if (leftoverCombinations.length == 0) {
            return leftoverTasks;
        }

        // We start by looping through all our remaining combinations
        for (int i = 0; i < leftoverCombinations.length; i++) {
            for (int j = 0; j < leftoverCombinations[i].getTasks().length; j++) {
                // If our task isn't already collected in our leftover tasks TaskCombination (checked through the task
                // string), then we add it on
                if (!leftoverTasks.getTasksString().contains(leftoverCombinations[i].getTasks()[j].getTaskName())) {
                    leftoverTasks.addTask(leftoverCombinations[i].getTasks()[j]);
                }
            }
        }

        return leftoverTasks;
    }
}
