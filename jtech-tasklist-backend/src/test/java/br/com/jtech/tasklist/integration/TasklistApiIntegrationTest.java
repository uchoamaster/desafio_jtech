package br.com.jtech.tasklist.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TasklistApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterLoginRefreshAndManageTasklistsWithJwt() throws Exception {
        var registerResponse = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Angelo",
                                  "email": "angelo@tasklist.local",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andReturn();

        var registerJson = readJson(registerResponse.getResponse().getContentAsString());
        var refreshToken = registerJson.get("refreshToken").asText();

        var loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "angelo@tasklist.local",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andReturn();

        var accessToken = readJson(loginResponse.getResponse().getContentAsString()).get("token").asText();

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.refreshToken").isString());

        var createListResponse = mockMvc.perform(post("/api/v1/tasklists")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Trabalho"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Trabalho"))
                .andReturn();

        var listId = readJson(createListResponse.getResponse().getContentAsString()).get("id").asText();

        var createTaskResponse = mockMvc.perform(post("/api/v1/tasklists/{listId}/tasks", listId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Revisar PR",
                                  "notes": "Cobrir criterios do teste"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tasks[0].title").value("Revisar PR"))
                .andReturn();

        var taskId = readJson(createTaskResponse.getResponse().getContentAsString()).get("tasks").get(0).get("id").asText();

        mockMvc.perform(patch("/api/v1/tasklists/{listId}/tasks/{taskId}/status", listId, taskId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "completed": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks[0].completed").value(true));

        mockMvc.perform(get("/api/v1/tasklists")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Trabalho"))
                .andExpect(jsonPath("$[0].tasks[0].title").value("Revisar PR"));

        var createTaskViaContract = mockMvc.perform(post("/tasks")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tasklistId": "%s",
                                  "title": "Publicar entrega",
                                  "notes": "Usando contrato /tasks"
                                }
                                """.formatted(listId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tasklistId").value(listId))
                .andExpect(jsonPath("$.title").value("Publicar entrega"))
                .andReturn();

        var directTaskId = readJson(createTaskViaContract.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/tasks")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(directTaskId));

        mockMvc.perform(get("/tasks/{taskId}", directTaskId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasklistName").value("Trabalho"));

        mockMvc.perform(put("/tasks/{taskId}", directTaskId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Publicar entrega final",
                                  "notes": "Contrato alinhado"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Publicar entrega final"));

        mockMvc.perform(patch("/tasks/{taskId}/status", directTaskId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "completed": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));

        mockMvc.perform(delete("/tasks/{taskId}", directTaskId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(put("/api/v1/tasklists/{listId}", listId)
                        .header("Authorization", "Bearer " + refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Nao deve passar"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();
    }

    @Test
    void shouldBlockCrossUserAccessToTasklistsAndTasks() throws Exception {
        var firstUserToken = registerAndLogin("Angelo", "angelo@tasklist.local", "123456");
        var secondUserToken = registerAndLogin("Maria", "maria@tasklist.local", "654321");

        var createListResponse = mockMvc.perform(post("/api/v1/tasklists")
                        .header("Authorization", "Bearer " + firstUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Privada"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        var listId = readJson(createListResponse.getResponse().getContentAsString()).get("id").asText();

        var createTaskResponse = mockMvc.perform(post("/tasks")
                        .header("Authorization", "Bearer " + firstUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tasklistId": "%s",
                                  "title": "Segredo",
                                  "notes": "Somente do primeiro usuario"
                                }
                                """.formatted(listId)))
                .andExpect(status().isCreated())
                .andReturn();

        var taskId = readJson(createTaskResponse.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/v1/tasklists")
                        .header("Authorization", "Bearer " + secondUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(get("/tasks")
                        .header("Authorization", "Bearer " + secondUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(put("/api/v1/tasklists/{listId}", listId)
                        .header("Authorization", "Bearer " + secondUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Invadida"
                                }
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/v1/tasklists/{listId}", listId)
                        .header("Authorization", "Bearer " + secondUserToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/tasks/{taskId}", taskId)
                        .header("Authorization", "Bearer " + secondUserToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/tasks/{taskId}", taskId)
                        .header("Authorization", "Bearer " + secondUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Alterada por outro usuario",
                                  "notes": "Nao deve acontecer"
                                }
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/tasks/{taskId}/status", taskId)
                        .header("Authorization", "Bearer " + secondUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "completed": true
                                }
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/tasks/{taskId}", taskId)
                        .header("Authorization", "Bearer " + secondUserToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/tasklists")
                        .header("Authorization", "Bearer " + firstUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Privada"));

        mockMvc.perform(get("/tasks")
                        .header("Authorization", "Bearer " + firstUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Segredo"))
                .andExpect(jsonPath("$[0].tasklistName").value("Privada"));
    }

    private String registerAndLogin(String name, String email, String password) throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(name, email, password)))
                .andExpect(status().isCreated());

        var loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();

        return readJson(loginResponse.getResponse().getContentAsString()).get("token").asText();
    }

    private JsonNode readJson(String value) throws Exception {
        return objectMapper.readTree(value);
    }
}