package com.bugtracking.server.domain.task;

import com.bugtracking.server.domain.exceptions.InvalidRequestException;
import com.bugtracking.server.domain.exceptions.ObjectNotFoundException;
import com.bugtracking.server.domain.project.Project;
import com.bugtracking.server.domain.project.ProjectRepository;
import com.bugtracking.server.dto.task.NewTaskSpec;
import com.bugtracking.server.dto.task.TaskSpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TaskCommandsFactory {

    private TaskRepository taskRepository;
    private ProjectRepository projectRepository;

    public TaskCommandsFactory(TaskRepository taskRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    public class CreateTaskCommand {

        private TaskRepository taskRepository;
        private ProjectRepository projectRepository;

        public CreateTaskCommand(TaskRepository taskRepository, ProjectRepository projectRepository) {
            this.taskRepository = taskRepository;
            this.projectRepository = projectRepository;
        }

        @Transactional
        public Task createTask(NewTaskSpec spec) throws ObjectNotFoundException {
            Project project = projectRepository.findById(spec.getProjectId()).orElse(null);
            if (project == null) {
                throw new ObjectNotFoundException(Project.class, spec.getProjectId());
            }
            Task task = new Task(spec);
            return taskRepository.save(task);
        }
    }

    public class UpdateTaskCommand {

        private TaskRepository taskRepository;

        public UpdateTaskCommand(TaskRepository taskRepository) {
            this.taskRepository = taskRepository;
        }

        @Transactional
        public Task updateTask(long taskId, TaskSpec spec) throws ObjectNotFoundException {
            Task task = taskRepository.findById(taskId).orElse(null);
            if (task == null) {
                throw new ObjectNotFoundException(Task.class, taskId);
            }
            if (task.getStatus() == TaskStatus.CLOSED) {
                throw new InvalidRequestException("You can not modify closed task.");
            }

            task.setName(spec.getName());
            task.setDescription(spec.getDescription());
            task.setPriority(spec.getPriority());
            task.setStatus(spec.getStatus());

            return taskRepository.save(task);
        }
    }

    public class DeleteTaskCommand {

        private TaskRepository taskRepository;

        public DeleteTaskCommand(TaskRepository taskRepository) {
            this.taskRepository = taskRepository;
        }

        @Transactional
        public void deleteTask(long taskId) throws ObjectNotFoundException {
            Task task = taskRepository.findById(taskId).orElse(null);
            if (task == null) {
                throw new ObjectNotFoundException(Task.class, taskId);
            }
            taskRepository.delete(task);
        }
    }

    @Bean
    @Scope("prototype")
    private TaskCommandsFactory.CreateTaskCommand createTaskCommand() {
        return new TaskCommandsFactory.CreateTaskCommand(taskRepository, projectRepository);
    }

    @Bean
    @Scope("prototype")
    private TaskCommandsFactory.UpdateTaskCommand updateTaskCommand() {
        return new TaskCommandsFactory.UpdateTaskCommand(taskRepository);
    }

    @Bean
    @Scope("prototype")
    private TaskCommandsFactory.DeleteTaskCommand deleteTaskCommand() {
        return new TaskCommandsFactory.DeleteTaskCommand(taskRepository);
    }
}
