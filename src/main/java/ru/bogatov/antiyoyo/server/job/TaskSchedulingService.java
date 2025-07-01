package ru.bogatov.antiyoyo.server.job;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class TaskSchedulingService {

    private final TaskScheduler taskScheduler;
    private final Map<String, ScheduledFuture<?>> scheduledTasks;

    public TaskSchedulingService(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
        this.scheduledTasks = new ConcurrentHashMap<>();
    }

    public void scheduleTask(String taskId, Runnable task, Instant executionTime) {
        ScheduledFuture<?> scheduledTask = taskScheduler.schedule(task, executionTime);
        scheduledTasks.put(taskId, scheduledTask);
    }



    public boolean containsTask(String taskId) {
        return scheduledTasks.containsKey(taskId);
    }

}
