package com.bugtracking.server.web.rest;

import com.bugtracking.server.domain.exceptions.ObjectAlreadyExistsException;
import com.bugtracking.server.domain.exceptions.ObjectNotFoundException;
import com.bugtracking.server.dto.OnlyIdDescription;
import com.bugtracking.server.dto.project.GetProjectsDescription;
import com.bugtracking.server.dto.project.NewProjectSpec;
import com.bugtracking.server.dto.project.ProjectDescription;
import com.bugtracking.server.dto.project.ProjectSpec;
import com.bugtracking.server.services.ProjectService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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

@Validated
@RestController
@RequestMapping(value = "/api/projects", produces = {"application/json"})
public class ProjectRestController {

    @Autowired
    private ProjectService projectService;

    @ApiResponses({
        @ApiResponse(code = 201, message = "New Project is successfully created")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<OnlyIdDescription> createProject(
        @Valid @RequestBody NewProjectSpec spec,
        UriComponentsBuilder uriComponentsBuilder
    ) throws ObjectAlreadyExistsException {
        Long projectId = projectService.createProject(spec);
        URI projectUri = uriComponentsBuilder.path("/api/projects/{projectId}").buildAndExpand(projectId).toUri();
        return ResponseEntity.created(projectUri).body(new OnlyIdDescription(projectId));
    }

    @ApiResponses({
        @ApiResponse(code = 204, message = "Project has been successfully updated"),
        @ApiResponse(code = 404, message = "Project with specified ID does not exist")
    })
    @RequestMapping(path = "{projectId}", method = RequestMethod.PUT, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> updateProject(
        @PathVariable("projectId") long projectId,
        @Valid @RequestBody ProjectSpec spec
    ) throws ObjectAlreadyExistsException, ObjectNotFoundException {
        projectService.updateProject(projectId, spec);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses({
        @ApiResponse(code = 204, message = "Project is successfully deleted"),
        @ApiResponse(code = 404, message = "Project with specified ID does not exist")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(path = "{projectId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteProject(@PathVariable("projectId") long projectId) throws ObjectNotFoundException {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Project with specified ID does not exist")
    })
    @RequestMapping(path = "{projectId}", method = RequestMethod.GET)
    public ResponseEntity<ProjectDescription> getProjectDetails(@PathVariable("projectId") long projectId) throws ObjectNotFoundException {
        ProjectDescription description  = projectService.getProjectDetails(projectId);
        return ResponseEntity.ok().body(description);
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = GetProjectsDescription.class)
    })
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<GetProjectsDescription> listProjects(
        @RequestParam(value = "limit", required = false) @Min(value = 1, message = "Page limit must not be less than one.") Integer limit,
        @RequestParam(value = "page", required = false) @Min(value = 0, message = "Page index must not be less than zero.") Integer page,
        @RequestParam(value = "sortOrder", required = false) Sort.Direction sortOrder
    ) {
        return ResponseEntity.ok(projectService.getProjects(limit, page, sortOrder));
    }
}
