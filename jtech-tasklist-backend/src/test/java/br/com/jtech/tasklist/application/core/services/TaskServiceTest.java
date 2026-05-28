package br.com.jtech.tasklist.application.core.services;

import br.com.jtech.tasklist.adapters.input.protocols.TaskCreateRequest;
import br.com.jtech.tasklist.adapters.input.protocols.TaskRequest;
import br.com.jtech.tasklist.adapters.input.protocols.TaskStatusRequest;
import br.com.jtech.tasklist.adapters.output.repositories.TaskRepository;
import br.com.jtech.tasklist.adapters.output.repositories.TasklistRepository;
import br.com.jtech.tasklist.adapters.output.repositories.entities.TaskItemEntity;
import br.com.jtech.tasklist.adapters.output.repositories.entities.TasklistEntity;
import br.com.jtech.tasklist.adapters.output.repositories.entities.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TasklistRepository tasklistRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createTaskShouldAttachTaskToOwnedList() {
        var currentUser = user();
        var tasklist = tasklist(currentUser);

        when(tasklistRepository.findByIdAndOwnerId(tasklist.getId(), currentUser.getId())).thenReturn(Optional.of(tasklist));
        when(taskRepository.existsByTasklistIdAndTitleIgnoreCase(tasklist.getId(), "Revisar PR")).thenReturn(false);
        when(taskRepository.save(any(TaskItemEntity.class))).thenAnswer(invocation -> {
            var entity = invocation.getArgument(0, TaskItemEntity.class);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        var response = taskService.createTask(currentUser, TaskCreateRequest.builder()
                .tasklistId(tasklist.getId().toString())
                .title(" Revisar PR ")
                .notes("Cobrir regressao")
                .build());

        var savedTask = ArgumentCaptor.forClass(TaskItemEntity.class);
        verify(taskRepository).save(savedTask.capture());

        assertThat(savedTask.getValue().getTasklist()).isEqualTo(tasklist);
        assertThat(savedTask.getValue().getOwner()).isEqualTo(currentUser);
        assertThat(savedTask.getValue().getTitle()).isEqualTo("Revisar PR");
        assertThat(response.getTasklistId()).isEqualTo(tasklist.getId().toString());
    }

    @Test
    void updateTaskShouldRejectDuplicateTitleInsideSameList() {
        var currentUser = user();
        var tasklist = tasklist(currentUser);
        var task = task(tasklist);

        when(taskRepository.findByIdAndOwnerId(task.getId(), currentUser.getId())).thenReturn(Optional.of(task));
        when(taskRepository.existsByTasklistIdAndTitleIgnoreCaseAndIdNot(tasklist.getId(), "Duplicada", task.getId())).thenReturn(true);

        assertThatThrownBy(() -> taskService.updateTask(currentUser, task.getId().toString(), TaskRequest.builder()
                .title("Duplicada")
                .notes("Outra")
                .build()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void findAllShouldReturnOnlyOwnedTasks() {
        var currentUser = user();
        var tasklist = tasklist(currentUser);
        var firstTask = task(tasklist);
        var secondTask = task(tasklist);
        secondTask.setId(UUID.randomUUID());
        secondTask.setTitle("Executar testes");

        when(taskRepository.findAllByOwnerIdOrderByCreatedAtDesc(currentUser.getId())).thenReturn(List.of(firstTask, secondTask));

        var response = taskService.findAll(currentUser);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).getTasklistName()).isEqualTo("Trabalho");
    }

    @Test
    void updateStatusShouldPersistCompletionFlag() {
        var currentUser = user();
        var task = task(tasklist(currentUser));

        when(taskRepository.findByIdAndOwnerId(task.getId(), currentUser.getId())).thenReturn(Optional.of(task));
        when(taskRepository.save(any(TaskItemEntity.class))).thenAnswer(invocation -> invocation.getArgument(0, TaskItemEntity.class));

        var response = taskService.updateStatus(currentUser, task.getId().toString(), TaskStatusRequest.builder().completed(true).build());

        assertThat(response.isCompleted()).isTrue();
    }

    private UserEntity user() {
        return UserEntity.builder()
                .id(UUID.randomUUID())
                .name("Angelo")
                .email("angelo@tasklist.local")
                .passwordHash("hashed-password")
                .build();
    }

    private TasklistEntity tasklist(UserEntity currentUser) {
        return TasklistEntity.builder()
                .id(UUID.randomUUID())
                .name("Trabalho")
                .owner(currentUser)
                .tasks(new ArrayList<>())
                .build();
    }

    private TaskItemEntity task(TasklistEntity tasklist) {
        return TaskItemEntity.builder()
                .id(UUID.randomUUID())
                .title("Revisar PR")
                .notes("Cobrir criterios")
                .completed(false)
                .tasklist(tasklist)
                .owner(tasklist.getOwner())
                .build();
    }
}