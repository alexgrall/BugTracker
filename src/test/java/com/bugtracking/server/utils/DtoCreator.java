package com.bugtracking.server.utils;

import com.bugtracking.server.domain.task.TaskStatus;
import com.bugtracking.server.dto.project.NewProjectSpec;
import com.bugtracking.server.dto.project.ProjectDescription;
import com.bugtracking.server.dto.task.NewTaskSpec;
import com.bugtracking.server.dto.task.TaskDescription;
import com.bugtracking.server.dto.task.TaskSpec;
import com.bugtracking.server.dto.task.TaskSpecBaseInfo;
import org.apache.commons.lang3.RandomUtils;

import java.time.LocalDateTime;
import java.util.UUID;

public class DtoCreator {

    public static NewProjectSpec createProjectSpec() {
        NewProjectSpec spec = new NewProjectSpec();
        spec.setName("Name_" + UUID.randomUUID().toString());
        spec.setDescription("Description_" + UUID.randomUUID().toString());
        return spec;
    }

    public static ProjectDescription createProjectDescription() {
        ProjectDescription description = new ProjectDescription();
        description.setId(RandomUtils.nextLong(1, 100000));
        description.setName("Name_" + UUID.randomUUID().toString());
        description.setDescription("Description_" + UUID.randomUUID().toString());
        description.setCreatedAt(LocalDateTime.now());
        description.setModifiedAt(LocalDateTime.now());
        return description;
    }

    public static NewTaskSpec createNewTaskSpec(Long projectId) {
        NewTaskSpec spec = new NewTaskSpec();
        spec.setProjectId(projectId);
        generateTaskSpecBaseInfo(spec);
        return spec;
    }

    public static TaskSpec createTaskSpec() {
        TaskSpec spec = new TaskSpec();
        spec.setStatus(TaskStatus.IN_PROGRESS);
        generateTaskSpecBaseInfo(spec);
        return spec;
    }

    public static TaskDescription createTaskDescription() {
        TaskDescription description = new TaskDescription();
        description.setId(RandomUtils.nextLong(1, 100000));
        description.setName("Name_" + UUID.randomUUID().toString());
        description.setDescription("Description_" + UUID.randomUUID().toString());
        description.setPriority(RandomUtils.nextInt(1, 100));
        description.setStatus(TaskStatus.NEW);
        description.setCreatedAt(LocalDateTime.now());
        description.setModifiedAt(LocalDateTime.now());
        return description;
    }

    private static void generateTaskSpecBaseInfo(TaskSpecBaseInfo taskSpecBaseInfo) {
        taskSpecBaseInfo.setName("Name_" + UUID.randomUUID().toString());
        taskSpecBaseInfo.setDescription("Description_" + UUID.randomUUID().toString());
        taskSpecBaseInfo.setPriority(RandomUtils.nextInt(1, 9999));
    }
}
