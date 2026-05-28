package br.com.jtech.tasklist.adapters.input.controllers;

import br.com.jtech.tasklist.adapters.input.protocols.TaskCreateRequest;
import br.com.jtech.tasklist.adapters.input.protocols.TaskRequest;
import br.com.jtech.tasklist.adapters.input.protocols.TaskResponse;
import br.com.jtech.tasklist.adapters.input.protocols.TaskStatusRequest;
import br.com.jtech.tasklist.adapters.output.repositories.entities.UserEntity;
import br.com.jtech.tasklist.application.core.services.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@AuthenticationPrincipal UserEntity currentUser,
                               @Valid @RequestBody TaskCreateRequest request) {
        return taskService.createTask(currentUser, request);
    }

    @GetMapping
    public List<TaskResponse> findAll(@AuthenticationPrincipal UserEntity currentUser) {
        return taskService.findAll(currentUser);
    }

    @GetMapping("/{taskId}")
    public TaskResponse findById(@AuthenticationPrincipal UserEntity currentUser,
                                 @PathVariable String taskId) {
        return taskService.findById(currentUser, taskId);
    }

    @PutMapping("/{taskId}")
    public TaskResponse update(@AuthenticationPrincipal UserEntity currentUser,
                               @PathVariable String taskId,
                               @Valid @RequestBody TaskRequest request) {
        return taskService.updateTask(currentUser, taskId, request);
    }

    @PatchMapping("/{taskId}/status")
    public TaskResponse updateStatus(@AuthenticationPrincipal UserEntity currentUser,
                                     @PathVariable String taskId,
                                     @RequestBody TaskStatusRequest request) {
        return taskService.updateStatus(currentUser, taskId, request);
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserEntity currentUser,
                       @PathVariable String taskId) {
        taskService.deleteTask(currentUser, taskId);
    }
}