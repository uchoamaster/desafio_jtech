package br.com.jtech.tasklist.controller;

import br.com.jtech.tasklist.controller.dto.TaskRequest;
import br.com.jtech.tasklist.controller.dto.TaskStatusRequest;
import br.com.jtech.tasklist.controller.dto.TasklistRequest;
import br.com.jtech.tasklist.controller.dto.TasklistResponse;
import br.com.jtech.tasklist.domain.UserEntity;
import br.com.jtech.tasklist.service.TasklistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasklists")
public class TasklistController {

    private final TasklistService tasklistService;

    public TasklistController(TasklistService tasklistService) {
        this.tasklistService = tasklistService;
    }

    @GetMapping
    public List<TasklistResponse> findAll(@AuthenticationPrincipal UserEntity currentUser) {
        return tasklistService.findAll(currentUser);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TasklistResponse createList(@AuthenticationPrincipal UserEntity currentUser,
                                       @Valid @RequestBody TasklistRequest request) {
        return tasklistService.createList(currentUser, request);
    }

    @PutMapping("/{listId}")
    public TasklistResponse updateList(@AuthenticationPrincipal UserEntity currentUser,
                                       @PathVariable String listId,
                                       @Valid @RequestBody TasklistRequest request) {
        return tasklistService.updateList(currentUser, listId, request);
    }

    @DeleteMapping("/{listId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteList(@AuthenticationPrincipal UserEntity currentUser,
                           @PathVariable String listId) {
        tasklistService.deleteList(currentUser, listId);
    }

    @PostMapping("/{listId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TasklistResponse createTask(@AuthenticationPrincipal UserEntity currentUser,
                                       @PathVariable String listId,
                                       @Valid @RequestBody TaskRequest request) {
        return tasklistService.createTask(currentUser, listId, request);
    }

    @PutMapping("/{listId}/tasks/{taskId}")
    public TasklistResponse updateTask(@AuthenticationPrincipal UserEntity currentUser,
                                       @PathVariable String listId,
                                       @PathVariable String taskId,
                                       @Valid @RequestBody TaskRequest request) {
        return tasklistService.updateTask(currentUser, listId, taskId, request);
    }

    @PatchMapping("/{listId}/tasks/{taskId}/status")
    public TasklistResponse updateTaskStatus(@AuthenticationPrincipal UserEntity currentUser,
                                             @PathVariable String listId,
                                             @PathVariable String taskId,
                                             @RequestBody TaskStatusRequest request) {
        return tasklistService.updateTaskStatus(currentUser, listId, taskId, request);
    }

    @DeleteMapping("/{listId}/tasks/{taskId}")
    public TasklistResponse deleteTask(@AuthenticationPrincipal UserEntity currentUser,
                                       @PathVariable String listId,
                                       @PathVariable String taskId) {
        return tasklistService.deleteTask(currentUser, listId, taskId);
    }
}