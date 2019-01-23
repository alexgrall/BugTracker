package com.bugtracking.server.dto.project;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ProjectSpec {

    private String name;
    private String description;

    @NotNull
    @Size(min = 1, max = 255)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Size(max = 255)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
