package com.bugtracking.server.rest;

import com.bugtracking.server.dto.task.NewTaskSpec;
import com.bugtracking.server.dto.task.TaskDescription;
import com.bugtracking.server.dto.task.TaskSpec;
import com.bugtracking.server.services.TaskService;
import com.bugtracking.server.utils.DtoCreator;
import com.bugtracking.server.web.rest.TaskRestController;
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

@WebMvcTest(TaskRestController.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TaskRestControllerTest {

    @Autowired
    private MockMvc mvc;
    @MockBean
    private TaskService taskService;

    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(Jackson2ObjectMapperBuilder objectMapperBuilder) {
        objectMapper = objectMapperBuilder.build();
    }

    @Test
    public void testCreate() throws Exception {
        Long expectedTaskId = RandomUtils.nextLong(1, 100000);
        NewTaskSpec spec = DtoCreator.createNewTaskSpec(RandomUtils.nextLong(1, 100000));

        given(taskService.createTask(nullable(NewTaskSpec.class))).willReturn(expectedTaskId);
        mvc.perform(post("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(objectMapper.writeValueAsString(spec)))
            .andExpect(status().isCreated())
            .andExpect(header().string(HttpHeaders.LOCATION, endsWith("/api/tasks/" + expectedTaskId)));

        ArgumentCaptor<NewTaskSpec> specCaptor = ArgumentCaptor.forClass(NewTaskSpec.class);
        verify(taskService).createTask(specCaptor.capture());
        assertThat(specCaptor.getValue()).isEqualToComparingFieldByFieldRecursively(spec);
    }

    @Test
    public void testUpdate() throws Exception {
        Long taskId = RandomUtils.nextLong(1, 100000);
        TaskSpec spec = DtoCreator.createTaskSpec();
        String specStr = objectMapper.writeValueAsString(spec);

        doNothing().when(taskService).updateTask(nullable(long.class), nullable(TaskSpec.class));
        mvc.perform(put("/api/tasks/{id}", taskId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(specStr))
            .andExpect(status().isNoContent())
            .andExpect(content().bytes(new byte[0]));

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<TaskSpec> specCaptor = ArgumentCaptor.forClass(TaskSpec.class);
        verify(taskService).updateTask(idCaptor.capture(), specCaptor.capture());
        assertThat(idCaptor.getValue()).isEqualTo(taskId);
        assertThat(specCaptor.getValue()).isEqualToComparingFieldByFieldRecursively(spec);
    }

    @Test
    public void testDelete() throws Exception {
        Long taskId = RandomUtils.nextLong(1, 100000);

        doNothing().when(taskService).deleteTask(nullable(long.class));
        mvc.perform(delete("/api/tasks/{id}", taskId))
            .andExpect(status().isNoContent());

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(taskService).deleteTask(idCaptor.capture());
        assertThat(idCaptor.getValue()).isEqualTo(taskId);
    }

    @Test
    public void testGetDetails() throws Exception {
        TaskDescription expectedDescription = DtoCreator.createTaskDescription();
        Long taskId = expectedDescription.getId();
        String expectedDescriptionStr = new String(objectMapper.writeValueAsBytes(expectedDescription), Charset.forName("UTF-8"));

        given(taskService.getTaskDetails(nullable(long.class))).willReturn(expectedDescription);
        mvc.perform(get("/api/tasks/{id}", taskId))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(expectedDescriptionStr));

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(taskService).getTaskDetails(idCaptor.capture());
        assertThat(idCaptor.getValue()).isEqualTo(taskId);
    }
}
