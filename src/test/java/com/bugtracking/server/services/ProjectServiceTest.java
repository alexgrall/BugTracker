package com.bugtracking.server.services;

import com.bugtracking.server.dto.project.GetProjectsDescription;
import com.bugtracking.server.dto.project.NewProjectSpec;
import com.bugtracking.server.dto.project.ProjectDescription;
import com.bugtracking.server.dto.project.ProjectSpec;
import com.bugtracking.server.utils.DtoCreator;
import org.junit.After;
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
public class ProjectServiceTest {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private JdbcTemplate jdbc = new JdbcTemplate();

    @After
    public void tearDown() {
        jdbc.update("DELETE FROM projects");
    }

    @Test
    public void testCreateProject() throws Exception {
        NewProjectSpec projectSpec = DtoCreator.createProjectSpec();
        Long projectId = projectService.createProject(projectSpec);
        assertThat(projectId).isNotNull();
        assertProject(projectId, projectSpec);
    }

    @Test
    public void testUpdateProject() throws Exception {
        NewProjectSpec newProjectSpec = DtoCreator.createProjectSpec();
        Long projectId = projectService.createProject(newProjectSpec);
        assertThat(projectId).isNotNull();

        ProjectSpec updateProjectSpec = DtoCreator.createProjectSpec();
        projectService.updateProject(projectId, updateProjectSpec);

        assertProject(projectId, updateProjectSpec);
    }

    @Test
    public void testDeleteProject() throws Exception {
        NewProjectSpec projectSpec = DtoCreator.createProjectSpec();
        Long projectId = projectService.createProject(projectSpec);
        assertThat(projectId).isNotNull();

        projectService.deleteProject(projectId);

        List<Map<String, Object>> queryResults = jdbc.queryForList("SELECT 1 FROM projects WHERE project_id = ?", projectId);
        assertThat(queryResults.size()).isEqualTo(0);
    }

    @Test
    public void testGetProjectDetails() throws Exception {
        NewProjectSpec newProjectSpec = DtoCreator.createProjectSpec();
        Long projectId = projectService.createProject(newProjectSpec);
        assertThat(projectId).isNotNull();

        ProjectDescription expected = generateExpectedProjectDescription(projectId, newProjectSpec);
        ProjectDescription projectDescription = projectService.getProjectDetails(projectId);
        assertProjectDetails(projectDescription, expected);

        ProjectSpec updateProjectSpec = DtoCreator.createProjectSpec();
        projectService.updateProject(projectId, updateProjectSpec);
        projectDescription = projectService.getProjectDetails(projectId);

        expected = generateExpectedProjectDescription(projectId, updateProjectSpec);
        assertProjectDetails(projectDescription, expected);
    }

    @Test
    public void testGetProjects() throws Exception {
        NewProjectSpec projectSpec1 = DtoCreator.createProjectSpec();
        NewProjectSpec projectSpec2 = DtoCreator.createProjectSpec();
        NewProjectSpec projectSpec3 = DtoCreator.createProjectSpec();

        projectSpec1.setName("t1" + projectSpec1.getName());
        projectSpec2.setName("t2" + projectSpec2.getName());
        projectSpec3.setName("t3" + projectSpec3.getName());

        Long projectId1 = projectService.createProject(projectSpec1);
        Long projectId2 = projectService.createProject(projectSpec2);
        Long projectId3 = projectService.createProject(projectSpec3);

        GetProjectsDescription result = projectService.getProjects(null, null, null);
        assertThat(result.getTotal()).isEqualTo(3);
        assertThat(result.getItems().size()).isEqualTo(3);

        assertProjectDetails(result.getItems().get(0), generateExpectedProjectDescription(projectId1, projectSpec1));
        assertProjectDetails(result.getItems().get(1), generateExpectedProjectDescription(projectId2, projectSpec2));
        assertProjectDetails(result.getItems().get(2), generateExpectedProjectDescription(projectId3, projectSpec3));

        result = projectService.getProjects(null, null, Sort.Direction.DESC);
        assertThat(result.getItems().get(0).getId()).isEqualTo(projectId3);
        assertThat(result.getItems().get(1).getId()).isEqualTo(projectId2);
        assertThat(result.getItems().get(2).getId()).isEqualTo(projectId1);

        result = projectService.getProjects(2, null, Sort.Direction.DESC);
        assertThat(result.getTotal()).isEqualTo(3);
        assertThat(result.getItems().size()).isEqualTo(2);
    }

    private static ProjectDescription generateExpectedProjectDescription(Long projectId, ProjectSpec spec) {
        ProjectDescription projectDescription = new ProjectDescription();
        projectDescription.setId(projectId);
        projectDescription.setName(spec.getName());
        projectDescription.setDescription(spec.getDescription());
        return projectDescription;
    }

    private static void assertProjectDetails(ProjectDescription projectDescription, ProjectDescription expected) {
        assertThat(projectDescription.getId()).isEqualTo(expected.getId());
        assertThat(projectDescription.getName()).isEqualTo(expected.getName());
        assertThat(projectDescription.getDescription()).isEqualTo(expected.getDescription());
    }

    private void assertProject(Long projectId, ProjectSpec spec) {
        List<Map<String, Object>> queryResults = jdbc.queryForList("SELECT * FROM projects WHERE project_id = ?", projectId);
        assertThat(queryResults.size()).isEqualTo(1);
        assertThat(queryResults.get(0)).containsEntry("name", spec.getName());
        assertThat(queryResults.get(0)).containsEntry("description", spec.getDescription());
    }
}
