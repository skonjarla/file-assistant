package net.konjarla.aifilemonitor.controller;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.konjarla.aifilemonitor.monitoring.model.TaskStatus;
import net.konjarla.aifilemonitor.monitoring.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/index/tasks")
@AllArgsConstructor
@Slf4j
public class TaskController {
    @NonNull
    TaskService taskService;
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskStatus> getTaskStatus(@PathVariable String taskId) {
        TaskStatus status = taskService.getTaskStatus(taskId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getTaskStatusForAll() {
        Map<String, Object> status = taskService.getTaskStatusForAllAsync();
        return ResponseEntity.ok(status);
    }
}
