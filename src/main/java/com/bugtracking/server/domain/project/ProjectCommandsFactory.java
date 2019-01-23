package com.bugtracking.server.domain.project;

import com.bugtracking.server.domain.exceptions.ObjectAlreadyExistsException;
import com.bugtracking.server.domain.exceptions.ObjectNotFoundException;
import com.bugtracking.server.dto.project.NewProjectSpec;
import com.bugtracking.server.dto.project.ProjectSpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Component
public class ProjectCommandsFactory {

    private ProjectRepository projectRepository;

    public ProjectCommandsFactory(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    private void checkProjectNameUniqueness(String name, Long projectId) throws ObjectAlreadyExistsException {
        Project existingProject = projectRepository.findByName(name);
        if (existingProject != null && !existingProject.getId().equals(projectId)) {
            throw new ObjectAlreadyExistsException(Project.class, Collections.singletonMap("name", name), existingProject.getId());
        }
    }

    public class CreateProjectCommand {

        private ProjectRepository projectRepository;

        public CreateProjectCommand(ProjectRepository projectRepository) {
            this.projectRepository = projectRepository;
        }

        @Transactional
        public Project createProject(NewProjectSpec spec) throws ObjectAlreadyExistsException {
            checkProjectNameUniqueness(spec.getName(), null);
            Project project = new Project(spec);
            return projectRepository.save(project);
        }
    }

    public class UpdateProjectCommand {

        private ProjectRepository projectRepository;

        public UpdateProjectCommand(ProjectRepository projectRepository) {
            this.projectRepository = projectRepository;
        }

        @Transactional
        public Project updateProject(long projectId, ProjectSpec spec) throws ObjectNotFoundException, ObjectAlreadyExistsException {
            Project project = projectRepository.findById(projectId).orElse(null);
            if (project == null) {
                throw new ObjectNotFoundException(Project.class, projectId);
            }
            checkProjectNameUniqueness(spec.getName(), projectId);

            project.setName(spec.getName());
            project.setDescription(spec.getDescription());

            return projectRepository.save(project);
        }
    }

    public class DeleteProjectCommand {

        private ProjectRepository projectRepository;

        public DeleteProjectCommand(ProjectRepository projectRepository) {
            this.projectRepository = projectRepository;
        }

        @Transactional
        public void deleteProject(long projectId) throws ObjectNotFoundException {
            Project project = projectRepository.findById(projectId).orElse(null);
            if (project == null) {
                throw new ObjectNotFoundException(Project.class, projectId);
            }
            projectRepository.delete(project);
        }
    }

    @Bean
    @Scope("prototype")
    private ProjectCommandsFactory.CreateProjectCommand createProjectCommand() {
        return new ProjectCommandsFactory.CreateProjectCommand(projectRepository);
    }

    @Bean
    @Scope("prototype")
    private ProjectCommandsFactory.UpdateProjectCommand updateProjectCommand() {
        return new ProjectCommandsFactory.UpdateProjectCommand(projectRepository);
    }

    @Bean
    @Scope("prototype")
    private ProjectCommandsFactory.DeleteProjectCommand deleteProjectCommand() {
        return new ProjectCommandsFactory.DeleteProjectCommand(projectRepository);
    }
}
