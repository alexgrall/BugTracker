package com.bugtracking.server.services;

import com.bugtracking.server.domain.task.TaskStatus;
import com.bugtracking.server.dto.TaskOrderFormat;
import com.bugtracking.server.dto.task.GetTasksDescription;
import com.bugtracking.server.dto.task.NewTaskSpec;
import com.bugtracking.server.dto.task.TaskDescription;
import com.bugtracking.server.dto.task.TaskSpec;
import com.bugtracking.server.dto.task.TaskSpecBaseInfo;
import com.bugtracking.server.utils.DtoCreator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class TaskServiceTest {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private JdbcTemplate jdbc = new JdbcTemplate();

    private Long projectId;

    @Before
    public void setUp() throws Exception {
        tearDown();
        projectId = projectService.createProject(DtoCreator.createProjectSpec());
    }

    @After
    public void tearDown() {
        jdbc.update("DELETE FROM tasks");
        jdbc.update("DELETE FROM projects");
    }

    @Test
    public void testCreateTask() throws Exception {
        NewTaskSpec taskSpec = DtoCreator.createNewTaskSpec(projectId);
        Long taskId = taskService.createTask(taskSpec);
        assertThat(taskId).isNotNull();
        assertTask(taskId, taskSpec);
    }

    @Test
    public void testUpdateTask() throws Exception {
        NewTaskSpec newTaskSpec = DtoCreator.createNewTaskSpec(projectId);
        Long taskId = taskService.createTask(newTaskSpec);
        assertThat(taskId).isNotNull();

        TaskSpec updateTaskSpec = DtoCreator.createTaskSpec();
        taskService.updateTask(taskId, updateTaskSpec);

        assertTask(taskId, updateTaskSpec);
    }

    @Test
    public void testGetTaskDetails() throws Exception {
        NewTaskSpec newTaskSpec = DtoCreator.createNewTaskSpec(projectId);
        Long taskId = taskService.createTask(newTaskSpec);
        assertThat(taskId).isNotNull();

        TaskDescription expected = generateExpectedTaskDescription(taskId, newTaskSpec, projectId, TaskStatus.NEW);
        TaskDescription taskDescription = taskService.getTaskDetails(taskId);
        assertTaskDetails(taskDescription, expected);

        TaskSpec updateTaskSpec = DtoCreator.createTaskSpec();
        taskService.updateTask(taskId, updateTaskSpec);
        taskDescription = taskService.getTaskDetails(taskId);

        expected = generateExpectedTaskDescription(taskId, updateTaskSpec, projectId, updateTaskSpec.getStatus());
        assertTaskDetails(taskDescription, expected);
    }

    @Test
    public void testGetTasks() throws Exception {
        NewTaskSpec newTaskSpec1 = DtoCreator.createNewTaskSpec(projectId);
        NewTaskSpec newTaskSpec2 = DtoCreator.createNewTaskSpec(projectId);
        NewTaskSpec newTaskSpec3 = DtoCreator.createNewTaskSpec(projectId);

        newTaskSpec1.setPriority(1);
        newTaskSpec2.setPriority(2);
        newTaskSpec3.setPriority(3);

        Long taskId1 = taskService.createTask(newTaskSpec1);
        Long taskId2 = taskService.createTask(newTaskSpec2);
        Long taskId3 = taskService.createTask(newTaskSpec3);

        GetTasksDescription result = taskService.getTasks(null, null, null, TaskOrderFormat.PRIORITY, null, null, null, null, null);
        assertThat(result.getTotal()).isEqualTo(3);
        assertThat(result.getItems().size()).isEqualTo(3);

        assertTaskDetails(result.getItems().get(0), generateExpectedTaskDescription(taskId1, newTaskSpec1, projectId, TaskStatus.NEW));
        assertTaskDetails(result.getItems().get(1), generateExpectedTaskDescription(taskId2, newTaskSpec2, projectId, TaskStatus.NEW));
        assertTaskDetails(result.getItems().get(2), generateExpectedTaskDescription(taskId3, newTaskSpec3, projectId, TaskStatus.NEW));

        result = taskService.getTasks(2, null, Sort.Direction.DESC, TaskOrderFormat.PRIORITY, null, null, null, null, null);
        assertThat(result.getTotal()).isEqualTo(3);
        assertThat(result.getItems().size()).isEqualTo(2);
        assertThat(result.getItems().get(0).getId()).isEqualTo(taskId3);

        Long anotherProjectId = projectService.createProject(DtoCreator.createProjectSpec());
        NewTaskSpec anotherTaskSpec = DtoCreator.createNewTaskSpec(anotherProjectId);
        taskService.createTask(anotherTaskSpec);

        result = taskService.getTasks(null, null, null, null, anotherProjectId, null, null, null, null);
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getItems().size()).isEqualTo(1);
    }

    @Test
    public void testDeleteTask() throws Exception {
        NewTaskSpec taskSpec = DtoCreator.createNewTaskSpec(projectId);
        Long taskId = taskService.createTask(taskSpec);
        assertThat(taskId).isNotNull();

        taskService.deleteTask(taskId);

        List<Map<String, Object>> queryResults = jdbc.queryForList("SELECT 1 FROM tasks WHERE task_id = ?", taskId);
        assertThat(queryResults.size()).isEqualTo(0);
    }

    private static TaskDescription generateExpectedTaskDescription(Long taskId, TaskSpecBaseInfo spec, Long projectId, TaskStatus status) {
        TaskDescription taskDescription = new TaskDescription();
        taskDescription.setId(taskId);
        taskDescription.setProjectId(projectId);
        taskDescription.setStatus(status);
        taskDescription.setPriority(spec.getPriority());
        taskDescription.setName(spec.getName());
        taskDescription.setDescription(spec.getDescription());
        return taskDescription;
    }

    private static void assertTaskDetails(TaskDescription taskDescription, TaskDescription expected) {
        assertThat(taskDescription.getId()).isEqualTo(expected.getId());
        assertThat(taskDescription.getProjectId()).isEqualTo(expected.getProjectId());
        assertThat(taskDescription.getName()).isEqualTo(expected.getName());
        assertThat(taskDescription.getDescription()).isEqualTo(expected.getDescription());
        assertThat(taskDescription.getPriority()).isEqualTo(expected.getPriority());
        assertThat(taskDescription.getStatus()).isEqualTo(expected.getStatus());
    }

    private void assertTask(Long taskId, TaskSpecBaseInfo spec) {
        List<Map<String, Object>> queryResults = jdbc.queryForList("SELECT * FROM tasks WHERE task_id = ?", taskId);
        assertThat(queryResults.size()).isEqualTo(1);
        assertThat(queryResults.get(0)).containsEntry("name", spec.getName());
        assertThat(queryResults.get(0)).containsEntry("description", spec.getDescription());
        assertThat(queryResults.get(0)).containsEntry("priority", spec.getPriority());
        if (spec instanceof NewTaskSpec) {
            assertThat(queryResults.get(0)).containsEntry("status", TaskStatus.NEW.name());
            assertThat(queryResults.get(0)).containsEntry("project_id", ((NewTaskSpec) spec).getProjectId());
        }
        if (spec instanceof TaskSpec) {
            assertThat(queryResults.get(0)).containsEntry("status", ((TaskSpec) spec).getStatus().name());
        }
    }
}
