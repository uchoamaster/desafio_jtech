package br.com.jtech.tasklist.service;

import br.com.jtech.tasklist.controller.dto.TaskCreateRequest;
import br.com.jtech.tasklist.controller.dto.TaskRequest;
import br.com.jtech.tasklist.controller.dto.TaskResponse;
import br.com.jtech.tasklist.controller.dto.TaskStatusRequest;
import br.com.jtech.tasklist.domain.TaskItemEntity;
import br.com.jtech.tasklist.domain.TasklistEntity;
import br.com.jtech.tasklist.domain.UserEntity;
import br.com.jtech.tasklist.repository.TaskRepository;
import br.com.jtech.tasklist.repository.TasklistRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TasklistRepository tasklistRepository;

    public TaskService(TaskRepository taskRepository, TasklistRepository tasklistRepository) {
        this.taskRepository = taskRepository;
        this.tasklistRepository = tasklistRepository;
    }

    public TaskResponse createTask(UserEntity currentUser, TaskCreateRequest request) {
        var tasklist = getOwnedTasklist(currentUser, request.tasklistId());
        var normalizedTitle = request.title().trim();

        if (taskRepository.existsByTasklistIdAndTitleIgnoreCase(tasklist.getId(), normalizedTitle)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A task with this title already exists in the list.");
        }

        var task = new TaskItemEntity();
        task.setTitle(normalizedTitle);
        task.setNotes(request.notes() == null ? "" : request.notes().trim());
        task.setCompleted(false);
        task.setTasklist(tasklist);
        task.setOwner(currentUser);

        task = taskRepository.save(task);

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
        var normalizedTitle = request.title().trim();

        if (taskRepository.existsByTasklistIdAndTitleIgnoreCaseAndIdNot(task.getTasklist().getId(), normalizedTitle, task.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A task with this title already exists in the list.");
        }

        task.setTitle(normalizedTitle);
        task.setNotes(request.notes() == null ? "" : request.notes().trim());

        return TaskResponse.of(taskRepository.save(task));
    }

    public TaskResponse updateStatus(UserEntity currentUser, String taskId, TaskStatusRequest request) {
        var task = getOwnedTask(currentUser, taskId);
        task.setCompleted(request.completed());
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