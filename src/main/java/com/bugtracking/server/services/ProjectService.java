package com.bugtracking.server.services;

import com.bugtracking.server.domain.exceptions.ObjectAlreadyExistsException;
import com.bugtracking.server.domain.exceptions.ObjectNotFoundException;
import com.bugtracking.server.domain.project.Project;
import com.bugtracking.server.domain.project.ProjectCommandsFactory;
import com.bugtracking.server.domain.project.ProjectRepository;
import com.bugtracking.server.dto.project.GetProjectsDescription;
import com.bugtracking.server.dto.project.NewProjectSpec;
import com.bugtracking.server.dto.project.ProjectDescription;
import com.bugtracking.server.dto.project.ProjectSpec;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class ProjectService {

    private ProjectRepository projectRepository;
    private ProjectCommandsFactory.CreateProjectCommand createProjectCommand;
    private ProjectCommandsFactory.UpdateProjectCommand updateProjectCommand;
    private ProjectCommandsFactory.DeleteProjectCommand deleteProjectCommand;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectCommandsFactory.CreateProjectCommand createProjectCommand,
                          ProjectCommandsFactory.UpdateProjectCommand updateProjectCommand,
                          ProjectCommandsFactory.DeleteProjectCommand deleteProjectCommand
    ) {
        this.projectRepository = projectRepository;
        this.createProjectCommand = createProjectCommand;
        this.updateProjectCommand = updateProjectCommand;
        this.deleteProjectCommand = deleteProjectCommand;
    }

    public Long createProject(NewProjectSpec spec) throws ObjectAlreadyExistsException {
        return createProjectCommand.createProject(spec).getId();
    }

    public void updateProject(long id, ProjectSpec spec) throws ObjectAlreadyExistsException, ObjectNotFoundException {
        updateProjectCommand.updateProject(id, spec);
    }

    public void deleteProject(long id) throws ObjectNotFoundException {
        deleteProjectCommand.deleteProject(id);
    }

    @Transactional(readOnly = true)
    public ProjectDescription getProjectDetails(long id) throws ObjectNotFoundException {
        Project project = projectRepository.findById(id).orElse(null);
        if (project == null) {
            throw new ObjectNotFoundException(Project.class, id);
        }

        return project.toDescription(new ProjectDescription());
    }

    @Transactional(readOnly = true)
    public GetProjectsDescription getProjects(Integer limit, Integer pageNumber, Sort.Direction sortOrder) {
        PageRequest pageable = producePageRequest(limit, pageNumber, sortOrder);
        Page<Project> projectPage = projectRepository.findAllByFilters(pageable);

        GetProjectsDescription result = new GetProjectsDescription();
        result.setTotal(projectPage.getTotalElements());
        result.setItems(projectPage.stream()
            .map(project -> project.toDescription(new ProjectDescription()))
            .collect(Collectors.toList()));
        return result;
    }

    private PageRequest producePageRequest(Integer limit, Integer pageNumber, Sort.Direction sortOrder) {
        if (limit != null || pageNumber != null || sortOrder != null) {
            limit = (limit == null) ? 1000 : limit;
            pageNumber = (pageNumber == null) ? 0 : pageNumber;
            sortOrder = (sortOrder == null) ? Sort.Direction.ASC : sortOrder;
            Sort sort = Sort.by(new Sort.Order(sortOrder, "name"));
            return PageRequest.of(pageNumber, limit, sort);
        }
        return null;
    }
}
