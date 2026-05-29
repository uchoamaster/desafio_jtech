/*
*  @(#)TasklistResponse.java
*
*  Copyright (c) J-Tech Solucoes em Informatica.
*  All Rights Reserved.
*
*  This software is the confidential and proprietary information of J-Tech.
*  ("Confidential Information"). You shall not disclose such Confidential
*  Information and shall use it only in accordance with the terms of the
*  license agreement you entered into with J-Tech.
*
*/
package br.com.jtech.tasklist.controller.dto;

import br.com.jtech.tasklist.domain.TaskItemEntity;
import br.com.jtech.tasklist.domain.TasklistEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

/**
* class TasklistResponse 
* 
* user angelo.vicente 
*/
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record TasklistResponse(
    String id,
    String name,
    List<TaskItemResponse> tasks
) implements Serializable {

    public static TasklistResponse of(TasklistEntity entity) {
    return new TasklistResponse(
        entity.getId().toString(),
        entity.getName(),
        entity.getTasks().stream().map(TaskItemResponse::of).toList()
    );
    }

    public static TasklistResponse of(List<TasklistEntity> entities) {
        throw new UnsupportedOperationException("Use stream mapping for list responses");
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
        public record TaskItemResponse(
            String id,
            String title,
            String notes,
            boolean completed
        ) implements Serializable {

        public static TaskItemResponse of(TaskItemEntity entity) {
            return new TaskItemResponse(
                entity.getId().toString(),
                entity.getTitle(),
                entity.getNotes(),
                entity.isCompleted()
            );
        }
    }
}