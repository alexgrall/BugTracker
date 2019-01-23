package com.bugtracking.server.domain.task;

import com.bugtracking.server.dto.task.NewTaskSpec;
import com.bugtracking.server.dto.task.TaskDescription;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "task_id")
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 65535, columnDefinition = "TEXT")
    private String description;

    @Column(name = "priority", nullable = false)
    private int priority;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "enum")
    private TaskStatus status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_at", insertable = false, updatable = false)
    private LocalDateTime modifiedAt;

    public Task(NewTaskSpec spec) {
        projectId = spec.getProjectId();
        name = spec.getName();
        description = spec.getDescription();
        priority = spec.getPriority();
        status = TaskStatus.NEW;
    }

    Task() {
    }

    public Long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public <S extends TaskDescription> S toDescription(S taskDescription) {
        taskDescription.setId(id);
        taskDescription.setProjectId(projectId);
        taskDescription.setName(name);
        taskDescription.setDescription(description);
        taskDescription.setPriority(priority);
        taskDescription.setStatus(status);
        taskDescription.setCreatedAt(createdAt);
        taskDescription.setModifiedAt(modifiedAt);
        return taskDescription;
    }
}
