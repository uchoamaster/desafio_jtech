package br.com.jtech.tasklist.service;

import br.com.jtech.tasklist.controller.dto.TaskRequest;
import br.com.jtech.tasklist.controller.dto.TaskStatusRequest;
import br.com.jtech.tasklist.controller.dto.TasklistRequest;
import br.com.jtech.tasklist.controller.dto.TasklistResponse;
import br.com.jtech.tasklist.domain.TaskItemEntity;
import br.com.jtech.tasklist.domain.TasklistEntity;
import br.com.jtech.tasklist.domain.UserEntity;
import br.com.jtech.tasklist.repository.TasklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TasklistService {

    private final TasklistRepository tasklistRepository;

    public List<TasklistResponse> findAll(UserEntity currentUser) {
        return tasklistRepository.findAllByOwnerIdOrderByCreatedAtAsc(currentUser.getId())
                .stream()
                .map(TasklistResponse::of)
                .toList();
    }

    public TasklistResponse createList(UserEntity currentUser, TasklistRequest request) {
        var normalizedName = request.getName().trim();

        if (tasklistRepository.existsByOwnerIdAndNameIgnoreCase(currentUser.getId(), normalizedName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A list with this name already exists.");
        }

        var entity = tasklistRepository.save(TasklistEntity.builder()
                .name(normalizedName)
                .owner(currentUser)
                .build());

        return TasklistResponse.of(entity);
    }

    public TasklistResponse updateList(UserEntity currentUser, String listId, TasklistRequest request) {
        var entity = getOwnedList(currentUser, listId);
        var normalizedName = request.getName().trim();

        if (tasklistRepository.existsByOwnerIdAndNameIgnoreCaseAndIdNot(currentUser.getId(), normalizedName, entity.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Choose another name for the list.");
        }

        entity.setName(normalizedName);
        return TasklistResponse.of(tasklistRepository.save(entity));
    }

    public void deleteList(UserEntity currentUser, String listId) {
        var entity = getOwnedList(currentUser, listId);

        if (!entity.getTasks().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Remove tasks before deleting the list.");
        }

        tasklistRepository.delete(entity);
    }

    public TasklistResponse createTask(UserEntity currentUser, String listId, TaskRequest request) {
        var entity = getOwnedList(currentUser, listId);
        var normalizedTitle = request.getTitle().trim();

        ensureTaskTitleAvailable(entity, normalizedTitle, null);

        entity.addTask(TaskItemEntity.builder()
                .title(normalizedTitle)
                .notes(request.getNotes() == null ? "" : request.getNotes().trim())
                .completed(false)
                .build());

        return TasklistResponse.of(tasklistRepository.save(entity));
    }

    public TasklistResponse updateTask(UserEntity currentUser, String listId, String taskId, TaskRequest request) {
        var entity = getOwnedList(currentUser, listId);
        var task = getOwnedTask(entity, taskId);
        var normalizedTitle = request.getTitle().trim();

        ensureTaskTitleAvailable(entity, normalizedTitle, task.getId());

        task.setTitle(normalizedTitle);
        task.setNotes(request.getNotes() == null ? "" : request.getNotes().trim());

        return TasklistResponse.of(tasklistRepository.save(entity));
    }

    public TasklistResponse updateTaskStatus(UserEntity currentUser, String listId, String taskId, TaskStatusRequest request) {
        var entity = getOwnedList(currentUser, listId);
        var task = getOwnedTask(entity, taskId);

        task.setCompleted(request.isCompleted());
        return TasklistResponse.of(tasklistRepository.save(entity));
    }

    public TasklistResponse deleteTask(UserEntity currentUser, String listId, String taskId) {
        var entity = getOwnedList(currentUser, listId);
        var task = getOwnedTask(entity, taskId);

        entity.removeTask(task);
        return TasklistResponse.of(tasklistRepository.save(entity));
    }

    private TasklistEntity getOwnedList(UserEntity currentUser, String listId) {
        return tasklistRepository.findByIdAndOwnerId(UUID.fromString(listId), currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task list not found."));
    }

    private TaskItemEntity getOwnedTask(TasklistEntity list, String taskId) {
        return list.getTasks().stream()
                .filter(task -> task.getId().equals(UUID.fromString(taskId)))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found."));
    }

    private void ensureTaskTitleAvailable(TasklistEntity list, String normalizedTitle, UUID ignoredTaskId) {
        var duplicate = list.getTasks().stream()
                .anyMatch(task -> !task.getId().equals(ignoredTaskId) && task.getTitle().equalsIgnoreCase(normalizedTitle));

        if (duplicate) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A task with this title already exists in the list.");
        }
    }
}