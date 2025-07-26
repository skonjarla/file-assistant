package net.konjarla.aifilemonitor.monitoring.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.konjarla.aifilemonitor.fileprocessor.FileProcessor;
import net.konjarla.aifilemonitor.monitoring.model.FileTask;
import net.konjarla.aifilemonitor.monitoring.model.TaskRunStatus;
import net.konjarla.aifilemonitor.monitoring.model.TaskStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileProcessorTaskService {
    @NonNull
    TaskService taskService;
    @NonNull
    FileProcessor fileProcessor;

    @Async("indexTaskExecutor")
    public void performAsyncProcessFile(File file) {
        String taskId = UUID.randomUUID().toString();
        String threadName = Thread.currentThread().getName();
        String filePath = file.getAbsolutePath();
        FileTask fileTask = FileTask.builder()
                .taskId(taskId)
                .threadName(threadName)
                .filePath(filePath)
                .build();
        TaskStatus taskStatus = new TaskStatus(TaskRunStatus.RUNNING, fileTask);
        taskService.addTask(taskId, taskStatus);
        try {
            fileProcessor.processFile(file);
            taskService.updateTaskWithCompletion(taskId);
        } catch (Exception e) {
            log.error("Error processing file: " + file.getAbsolutePath(), e);
            taskService.updateTaskWithFailure(taskId, e.getMessage());
        }
    }

    public void performAsyncDeleteFile(File file) {
        String taskId = UUID.randomUUID().toString();
        String threadName = Thread.currentThread().getName();
        String filePath = file.getAbsolutePath();
        FileTask fileTask = FileTask.builder()
                .taskId(taskId)
                .threadName(threadName)
                .filePath(filePath)
                .build();
        TaskStatus taskStatus = new TaskStatus(TaskRunStatus.RUNNING, fileTask);
        taskService.addTask(taskId, taskStatus);
        try {
            fileProcessor.removeFile(file);
            taskService.updateTaskWithCompletion(taskId);
        } catch (Exception e) {
            log.error("Error deleting file: " + file.getAbsolutePath(), e);
            taskService.updateTaskWithFailure(taskId, e.getMessage());
        }
    }
}
