package br.com.jtech.tasklist.application.core.services;

import br.com.jtech.tasklist.adapters.input.protocols.TaskCreateRequest;
import br.com.jtech.tasklist.adapters.input.protocols.TaskRequest;
import br.com.jtech.tasklist.adapters.input.protocols.TaskResponse;
import br.com.jtech.tasklist.adapters.input.protocols.TaskStatusRequest;
import br.com.jtech.tasklist.adapters.output.repositories.TaskRepository;
import br.com.jtech.tasklist.adapters.output.repositories.TasklistRepository;
import br.com.jtech.tasklist.adapters.output.repositories.entities.TaskItemEntity;
import br.com.jtech.tasklist.adapters.output.repositories.entities.TasklistEntity;
import br.com.jtech.tasklist.adapters.output.repositories.entities.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TasklistRepository tasklistRepository;

    public TaskResponse createTask(UserEntity currentUser, TaskCreateRequest request) {
        var tasklist = getOwnedTasklist(currentUser, request.getTasklistId());
        var normalizedTitle = request.getTitle().trim();

        if (taskRepository.existsByTasklistIdAndTitleIgnoreCase(tasklist.getId(), normalizedTitle)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A task with this title already exists in the list.");
        }

        var task = taskRepository.save(TaskItemEntity.builder()
                .title(normalizedTitle)
                .notes(request.getNotes() == null ? "" : request.getNotes().trim())
                .completed(false)
                .tasklist(tasklist)
            .owner(currentUser)
                .build());

        return TaskResponse.of(task);
    }

    public List<TaskResponse> findAll(UserEntity currentUser) {
        return taskRepository.findAllByOwnerIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(TaskResponse::of)
                .toList();
    }

    public TaskResponse findById(UserEntity currentUser, String taskId) {
        return TaskResponse.of(getOwnedTask(currentUser, taskId));
    }

    public TaskResponse updateTask(UserEntity currentUser, String taskId, TaskRequest request) {
        var task = getOwnedTask(currentUser, taskId);
        var normalizedTitle = request.getTitle().trim();

        if (taskRepository.existsByTasklistIdAndTitleIgnoreCaseAndIdNot(task.getTasklist().getId(), normalizedTitle, task.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A task with this title already exists in the list.");
        }

        task.setTitle(normalizedTitle);
        task.setNotes(request.getNotes() == null ? "" : request.getNotes().trim());

        return TaskResponse.of(taskRepository.save(task));
    }

    public TaskResponse updateStatus(UserEntity currentUser, String taskId, TaskStatusRequest request) {
        var task = getOwnedTask(currentUser, taskId);
        task.setCompleted(request.isCompleted());
        return TaskResponse.of(taskRepository.save(task));
    }

    public void deleteTask(UserEntity currentUser, String taskId) {
        var task = getOwnedTask(currentUser, taskId);
        taskRepository.delete(task);
    }

    private TaskItemEntity getOwnedTask(UserEntity currentUser, String taskId) {
        return taskRepository.findByIdAndOwnerId(UUID.fromString(taskId), currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found."));
    }

    private TasklistEntity getOwnedTasklist(UserEntity currentUser, String tasklistId) {
        return tasklistRepository.findByIdAndOwnerId(UUID.fromString(tasklistId), currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task list not found."));
    }
}