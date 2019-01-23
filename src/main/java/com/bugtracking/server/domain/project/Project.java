package com.bugtracking.server.domain.project;

import com.bugtracking.server.dto.project.NewProjectSpec;
import com.bugtracking.server.dto.project.ProjectDescription;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "project_id")
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_at", insertable = false, updatable = false)
    private LocalDateTime modifiedAt;

    public Project(NewProjectSpec spec) {
        name = spec.getName();
        description = spec.getDescription();
    }

    Project() {
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

    public <S extends ProjectDescription> S toDescription(S projectDescription) {
        projectDescription.setId(id);
        projectDescription.setName(name);
        projectDescription.setDescription(description);
        projectDescription.setCreatedAt(createdAt);
        projectDescription.setModifiedAt(modifiedAt);
        return projectDescription;
    }
}
