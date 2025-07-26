package net.konjarla.aifilemonitor.monitoring.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.konjarla.aifilemonitor.monitoring.model.FileTask;
import net.konjarla.aifilemonitor.monitoring.model.TaskRunStatus;
import net.konjarla.aifilemonitor.monitoring.model.TaskStatus;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Slf4j
@Service
public class TaskService {
    @NonNull
    private final ConcurrentHashMap<String, TaskStatus> taskStatuses;
    @NonNull
    TaskExecutor scanTaskExecutor;
    @NonNull
    TaskExecutor watcherTaskExecutor;
    @NonNull
    TaskExecutor indexTaskExecutor;

    public void addTask(String taskId, TaskStatus taskStatus) {
        taskStatuses.put(taskId, taskStatus);
    }

    public void updateTaskWithCompletion(String taskId) {
        TaskStatus taskStatus = taskStatuses.get(taskId);
        if (taskStatus != null) {
            taskStatuses.put(taskId, new TaskStatus(TaskRunStatus.COMPLETED, taskStatus.getTask()));
        } else {
            log.error("Task with id {} not found", taskId);
        }
    }

    public void updateTaskWithFailure(String taskId, String message) {
        TaskStatus taskStatus = taskStatuses.get(taskId);
        if (taskStatus != null) {
            FileTask fileTask = taskStatus.getTask();
            FileTask updatedFileTask = FileTask.builder()
                    .taskId(fileTask.getTaskId())
                    .threadName(fileTask.getThreadName())
                    .filePath(fileTask.getFilePath())
                    .message(message)
                    .build();
            taskStatuses.put(taskId, new TaskStatus(TaskRunStatus.FAILED, updatedFileTask));
        } else {
            log.error("Task with id {} not found", taskId);
        }
    }

    public TaskStatus getTaskStatus(String taskId) {
        return taskStatuses.getOrDefault(taskId, new TaskStatus(TaskRunStatus.PENDING));
    }

    private List<TaskStatus> getTaskStatusForAll() {
        return taskStatuses.values()
                .stream()
                .toList();
    }

    public Map<String, Object> getTaskStatusForAllAsync() {
        List<TaskStatus> taskStatuses = getTaskStatusForAll();
        Map<String, Integer> executorMap = new HashMap<>();
        ThreadPoolTaskExecutor scanTaskExecutor = (ThreadPoolTaskExecutor) this.scanTaskExecutor;
        ThreadPoolTaskExecutor watcherTaskExecutor = (ThreadPoolTaskExecutor) this.watcherTaskExecutor;
        ThreadPoolTaskExecutor indexTaskExecutor = (ThreadPoolTaskExecutor) this.indexTaskExecutor;
        executorMap.put("scanTaskExecutor", scanTaskExecutor.getQueueSize());
        executorMap.put("watcherTaskExecutor", watcherTaskExecutor.getQueueSize());
        executorMap.put("indexTaskExecutor", indexTaskExecutor.getQueueSize());
        return Map.of("taskStatuses", taskStatuses, "executorMap", executorMap);
    }
}

