package com.bugtracking.server.rest;

import com.bugtracking.server.dto.project.NewProjectSpec;
import com.bugtracking.server.dto.project.ProjectDescription;
import com.bugtracking.server.dto.project.ProjectSpec;
import com.bugtracking.server.services.ProjectService;
import com.bugtracking.server.utils.DtoCreator;
import com.bugtracking.server.web.rest.ProjectRestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectRestController.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ProjectRestControllerTest {

    @Autowired
    private MockMvc mvc;
    @MockBean
    private ProjectService projectService;

    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(Jackson2ObjectMapperBuilder objectMapperBuilder) {
        objectMapper = objectMapperBuilder.build();
    }

    @Test
    public void testCreate() throws Exception {
        Long expectedProjectId = RandomUtils.nextLong(1, 100000);
        NewProjectSpec spec = DtoCreator.createProjectSpec();

        given(projectService.createProject(nullable(NewProjectSpec.class))).willReturn(expectedProjectId);
        mvc.perform(post("/api/projects")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(objectMapper.writeValueAsString(spec)))
            .andExpect(status().isCreated())
            .andExpect(header().string(HttpHeaders.LOCATION, endsWith("/api/projects/" + expectedProjectId)));

        ArgumentCaptor<NewProjectSpec> specCaptor = ArgumentCaptor.forClass(NewProjectSpec.class);
        verify(projectService).createProject(specCaptor.capture());
        assertThat(specCaptor.getValue()).isEqualToComparingFieldByFieldRecursively(spec);
    }

    @Test
    public void testUpdate() throws Exception {
        Long projectId = RandomUtils.nextLong(1, 100000);
        NewProjectSpec spec = DtoCreator.createProjectSpec();
        String specStr = objectMapper.writeValueAsString(spec);

        doNothing().when(projectService).updateProject(nullable(long.class), nullable(ProjectSpec.class));
        mvc.perform(put("/api/projects/{id}", projectId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(specStr))
            .andExpect(status().isNoContent())
            .andExpect(content().bytes(new byte[0]));

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<ProjectSpec> specCaptor = ArgumentCaptor.forClass(ProjectSpec.class);
        verify(projectService).updateProject(idCaptor.capture(), specCaptor.capture());
        assertThat(idCaptor.getValue()).isEqualTo(projectId);
        assertThat(specCaptor.getValue()).isEqualToComparingFieldByFieldRecursively(spec);
    }

    @Test
    public void testDelete() throws Exception {
        Long projectId = RandomUtils.nextLong(1, 100000);

        doNothing().when(projectService).deleteProject(nullable(long.class));
        mvc.perform(delete("/api/projects/{id}", projectId))
            .andExpect(status().isNoContent());

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(projectService).deleteProject(idCaptor.capture());
        assertThat(idCaptor.getValue()).isEqualTo(projectId);
    }

    @Test
    public void testGetDetails() throws Exception {
        ProjectDescription expectedDescription = DtoCreator.createProjectDescription();
        Long projectId = expectedDescription.getId();
        String expectedDescriptionStr = new String(objectMapper.writeValueAsBytes(expectedDescription), Charset.forName("UTF-8"));

        given(projectService.getProjectDetails(nullable(long.class))).willReturn(expectedDescription);
        mvc.perform(get("/api/projects/{id}", projectId))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(expectedDescriptionStr));

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(projectService).getProjectDetails(idCaptor.capture());
        assertThat(idCaptor.getValue()).isEqualTo(projectId);
    }
}
