package net.konjarla.aifilemonitor.monitoring.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class TaskStatus {
    private final TaskRunStatus status;
    private final FileTask task;

    public TaskStatus(TaskRunStatus status, FileTask task) {
        this.status = status;
        this.task = task;
    }

    public TaskStatus(TaskRunStatus status) {
        this(status, null);
    }

}
