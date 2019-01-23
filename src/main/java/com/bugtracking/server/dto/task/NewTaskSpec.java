package com.bugtracking.server.dto.task;

import javax.validation.constraints.NotNull;

public class NewTaskSpec extends TaskSpecBaseInfo {

    private Long projectId;

    @NotNull
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
