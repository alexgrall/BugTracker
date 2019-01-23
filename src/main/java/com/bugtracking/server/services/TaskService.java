package com.bugtracking.server.services;

import com.bugtracking.server.domain.exceptions.ObjectNotFoundException;
import com.bugtracking.server.domain.task.Task;
import com.bugtracking.server.domain.task.TaskCommandsFactory;
import com.bugtracking.server.domain.task.TaskRepository;
import com.bugtracking.server.domain.task.TaskStatus;
import com.bugtracking.server.dto.TaskOrderFormat;
import com.bugtracking.server.dto.task.GetTasksDescription;
import com.bugtracking.server.dto.task.NewTaskSpec;
import com.bugtracking.server.dto.task.TaskDescription;
import com.bugtracking.server.dto.task.TaskSpec;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private TaskRepository taskRepository;
    private TaskCommandsFactory.CreateTaskCommand createTaskCommand;
    private TaskCommandsFactory.UpdateTaskCommand updateTaskCommand;
    private TaskCommandsFactory.DeleteTaskCommand deleteTaskCommand;

    public TaskService(TaskRepository taskRepository,
                       TaskCommandsFactory.CreateTaskCommand createTaskCommand,
                       TaskCommandsFactory.UpdateTaskCommand updateTaskCommand,
                       TaskCommandsFactory.DeleteTaskCommand deleteTaskCommand
    ) {
        this.taskRepository = taskRepository;
        this.createTaskCommand = createTaskCommand;
        this.updateTaskCommand = updateTaskCommand;
        this.deleteTaskCommand = deleteTaskCommand;
    }

    public Long createTask(NewTaskSpec spec) throws ObjectNotFoundException {
        return createTaskCommand.createTask(spec).getId();
    }

    public void updateTask(long id, TaskSpec spec) throws ObjectNotFoundException {
        updateTaskCommand.updateTask(id, spec);
    }

    public void deleteTask(long id) throws ObjectNotFoundException {
        deleteTaskCommand.deleteTask(id);
    }

    @Transactional(readOnly = true)
    public TaskDescription getTaskDetails(long id) throws ObjectNotFoundException {
        Task task = taskRepository.findById(id).orElse(null);
        if (task == null) {
            throw new ObjectNotFoundException(Task.class, id);
        }
        return task.toDescription(new TaskDescription());
    }

    @Transactional(readOnly = true)
    public GetTasksDescription getTasks(Integer limit, Integer pageNumber, Sort.Direction sortOrder,
                                        TaskOrderFormat orderBy, Long projectId, Set<TaskStatus> statusSet,
                                        Set<Integer> prioritySet, LocalDateTime startDate, LocalDateTime endDate
    ) {
        PageRequest pageable = producePageRequest(limit, pageNumber, sortOrder, orderBy);
        Page<Task> taskPage = taskRepository.findAllByFilters(projectId, statusSet, prioritySet, startDate, endDate, pageable);

        GetTasksDescription result = new GetTasksDescription();
        result.setTotal(taskPage.getTotalElements());
        result.setItems(taskPage.stream()
            .map(task -> task.toDescription(new TaskDescription()))
            .collect(Collectors.toList()));
        return result;
    }

    private PageRequest producePageRequest(Integer limit, Integer pageNumber, Sort.Direction sortOrder, TaskOrderFormat orderBy) {
        if (limit != null || pageNumber != null || sortOrder != null || orderBy != null) {
            limit = (limit == null) ? 1000 : limit;
            pageNumber = (pageNumber == null) ? 0 : pageNumber;
            sortOrder = (sortOrder == null) ? Sort.Direction.ASC : sortOrder;

            Sort sort = null;
            if (orderBy == null) {
                sort = Sort.by(new Sort.Order(sortOrder, "id"));
            } else {
                switch (orderBy) {
                    case DATE:
                        sort = Sort.by(new Sort.Order(sortOrder, "modifiedAt"));
                        break;
                    case PRIORITY:
                        sort = Sort.by(new Sort.Order(sortOrder, "priority"), new Sort.Order(sortOrder, "modifiedAt"));
                        break;
                }
            }

            return PageRequest.of(pageNumber, limit, sort);
        }
        return null;
    }
}
