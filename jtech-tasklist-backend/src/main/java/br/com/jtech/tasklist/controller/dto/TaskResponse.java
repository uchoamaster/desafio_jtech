package br.com.jtech.tasklist.controller.dto;

import br.com.jtech.tasklist.domain.TaskItemEntity;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskResponse(
    String id,
    String title,
    String notes,
    boolean completed,
    String tasklistId,
    String tasklistName
) implements Serializable {

    public static TaskResponse of(TaskItemEntity entity) {
    return new TaskResponse(
        entity.getId().toString(),
        entity.getTitle(),
        entity.getNotes(),
        entity.isCompleted(),
        entity.getTasklist().getId().toString(),
        entity.getTasklist().getName()
    );
    }
}