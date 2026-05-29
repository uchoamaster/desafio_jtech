package br.com.jtech.tasklist.service;

import br.com.jtech.tasklist.controller.dto.TaskRequest;
import br.com.jtech.tasklist.controller.dto.TasklistRequest;
import br.com.jtech.tasklist.domain.TaskItemEntity;
import br.com.jtech.tasklist.domain.TasklistEntity;
import br.com.jtech.tasklist.domain.UserEntity;
import br.com.jtech.tasklist.repository.TasklistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TasklistServiceTest {

    @Mock
    private TasklistRepository tasklistRepository;

    @InjectMocks
    private TasklistService tasklistService;

    @Test
    void createListShouldPersistListForCurrentUser() {
        var currentUser = user();
        var request = TasklistRequest.builder().name(" Trabalho ").build();

        when(tasklistRepository.existsByOwnerIdAndNameIgnoreCase(currentUser.getId(), "Trabalho")).thenReturn(false);
        when(tasklistRepository.save(any(TasklistEntity.class))).thenAnswer(invocation -> {
            var entity = invocation.getArgument(0, TasklistEntity.class);
            entity.setId(UUID.randomUUID());
            entity.setTasks(new ArrayList<>());
            return entity;
        });

        var response = tasklistService.createList(currentUser, request);

        var savedList = ArgumentCaptor.forClass(TasklistEntity.class);
        verify(tasklistRepository).save(savedList.capture());

        assertThat(savedList.getValue().getName()).isEqualTo("Trabalho");
        assertThat(savedList.getValue().getOwner()).isEqualTo(currentUser);
        assertThat(response.getName()).isEqualTo("Trabalho");
    }

    @Test
    void deleteListShouldRejectListsWithTasks() {
        var currentUser = user();
        var list = TasklistEntity.builder()
                .id(UUID.randomUUID())
                .name("Trabalho")
                .owner(currentUser)
                .tasks(new ArrayList<>())
                .build();
        list.addTask(TaskItemEntity.builder().title("Revisar PR").notes("").completed(false).build());

        when(tasklistRepository.findByIdAndOwnerId(list.getId(), currentUser.getId())).thenReturn(Optional.of(list));

        assertThatThrownBy(() -> tasklistService.deleteList(currentUser, list.getId().toString()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createTaskShouldRejectDuplicateTitlesInsideTheSameList() {
        var currentUser = user();
        var list = TasklistEntity.builder()
                .id(UUID.randomUUID())
                .name("Trabalho")
                .owner(currentUser)
                .tasks(new ArrayList<>())
                .build();
        list.addTask(TaskItemEntity.builder().id(UUID.randomUUID()).title("Revisar PR").notes("Critico").completed(false).build());

        when(tasklistRepository.findByIdAndOwnerId(list.getId(), currentUser.getId())).thenReturn(Optional.of(list));

        assertThatThrownBy(() -> tasklistService.createTask(currentUser, list.getId().toString(), TaskRequest.builder()
                .title("revisar pr")
                .notes("Duplicada")
                .build()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    private UserEntity user() {
        return UserEntity.builder()
                .id(UUID.randomUUID())
                .name("Angelo")
                .email("angelo@tasklist.local")
                .passwordHash("hashed-password")
                .build();
    }
}