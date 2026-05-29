package br.com.jtech.tasklist.controller.dto;

import br.com.jtech.tasklist.domain.TaskItemEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskResponse implements Serializable {
    private String id;
    private String title;
    private String notes;
    private boolean completed;
    private String tasklistId;
    private String tasklistName;

    public static TaskResponse of(TaskItemEntity entity) {
        return TaskResponse.builder()
                .id(entity.getId().toString())
                .title(entity.getTitle())
                .notes(entity.getNotes())
                .completed(entity.isCompleted())
                .tasklistId(entity.getTasklist().getId().toString())
                .tasklistName(entity.getTasklist().getName())
                .build();
    }
}