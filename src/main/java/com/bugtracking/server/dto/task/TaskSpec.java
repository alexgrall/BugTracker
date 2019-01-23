package com.bugtracking.server.dto.task;

import com.bugtracking.server.domain.task.TaskStatus;

import javax.validation.constraints.NotNull;

public class TaskSpec extends TaskSpecBaseInfo {

    private TaskStatus status;

    @NotNull
    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
