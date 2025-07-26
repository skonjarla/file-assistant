package net.konjarla.aifilemonitor.monitoring.model;

public enum TaskRunStatus {
    PENDING("PENDING"),
    RUNNING("RUNNING"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED");

    private String value;

    TaskRunStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    public static TaskRunStatus fromValue(String value) {
        for (TaskRunStatus b : TaskRunStatus.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}
