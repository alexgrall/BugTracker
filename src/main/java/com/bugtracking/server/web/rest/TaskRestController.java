package com.bugtracking.server.web.rest;

import com.bugtracking.server.domain.exceptions.ObjectNotFoundException;
import com.bugtracking.server.domain.task.TaskStatus;
import com.bugtracking.server.dto.OnlyIdDescription;
import com.bugtracking.server.dto.TaskOrderFormat;
import com.bugtracking.server.dto.task.GetTasksDescription;
import com.bugtracking.server.dto.task.NewTaskSpec;
import com.bugtracking.server.dto.task.TaskDescription;
import com.bugtracking.server.dto.task.TaskSpec;
import com.bugtracking.server.services.TaskService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Set;

@Validated
@RestController
@RequestMapping(value = "/api/tasks", produces = {"application/json"})
public class TaskRestController {

    @Autowired
    private TaskService taskService;

    @ApiResponses({
        @ApiResponse(code = 201, message = "New Task is successfully created")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<OnlyIdDescription> createTask(
        @Valid @RequestBody NewTaskSpec spec,
        UriComponentsBuilder uriComponentsBuilder
    ) throws ObjectNotFoundException {
        Long taskId = taskService.createTask(spec);
        URI taskUri = uriComponentsBuilder.path("/api/tasks/{taskId}").buildAndExpand(taskId).toUri();
        return ResponseEntity.created(taskUri).body(new OnlyIdDescription(taskId));
    }

    @ApiResponses({
        @ApiResponse(code = 204, message = "Task has been successfully updated"),
        @ApiResponse(code = 404, message = "Task with specified ID does not exist")
    })
    @RequestMapping(path = "{taskId}", method = RequestMethod.PUT, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> updateTask(
        @PathVariable("taskId") long taskId,
        @Valid @RequestBody TaskSpec spec
    ) throws ObjectNotFoundException {
        taskService.updateTask(taskId, spec);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses({
        @ApiResponse(code = 204, message = "Task is successfully deleted"),
        @ApiResponse(code = 404, message = "Task with specified ID does not exist")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(path = "{taskId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteTask(@PathVariable("taskId") long taskId) throws ObjectNotFoundException {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Task with specified ID does not exist")
    })
    @RequestMapping(path = "{taskId}", method = RequestMethod.GET)
    public ResponseEntity<TaskDescription> getTaskDetails(@PathVariable("taskId") long taskId) throws ObjectNotFoundException {
        TaskDescription description  = taskService.getTaskDetails(taskId);
        return ResponseEntity.ok().body(description);
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = GetTasksDescription.class)
    })
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<GetTasksDescription> listTasks(
        @RequestParam(value = "limit", required = false) @Min(value = 1, message = "Page limit must not be less than one.") Integer limit,
        @RequestParam(value = "page", required = false) @Min(value = 0, message = "Page index must not be less than zero.") Integer page,
        @RequestParam(value = "sortOrder", required = false) Sort.Direction sortOrder,
        @RequestParam(value = "orderBy", required = false) TaskOrderFormat orderBy,
        @RequestParam(value = "projectId", required = false) Long projectId,
        @RequestParam(value = "status", required = false) Set<TaskStatus> statusSet,
        @RequestParam(value = "priority", required = false) Set<@Min(value = 1, message = "Priority must be greater than or equal to 1.") Integer> prioritySet,
        @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        GetTasksDescription tasks = taskService.getTasks(limit, page, sortOrder, orderBy, projectId, statusSet, prioritySet, startDate, endDate);
        return ResponseEntity.ok(tasks);
    }
}
