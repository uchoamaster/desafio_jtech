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
package br.com.jtech.tasklist.adapters.input.protocols;

import br.com.jtech.tasklist.adapters.output.repositories.entities.TaskItemEntity;
import br.com.jtech.tasklist.adapters.output.repositories.entities.TasklistEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
* class TasklistResponse 
* 
* user angelo.vicente 
*/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TasklistResponse implements Serializable {
    private String id;
    private String name;
    private List<TaskItemResponse> tasks;

    public static TasklistResponse of(TasklistEntity entity) {
        return TasklistResponse.builder()
                .id(entity.getId().toString())
                .name(entity.getName())
                .tasks(entity.getTasks().stream().map(TaskItemResponse::of).toList())
                .build();
    }

    public static TasklistResponse of(List<TasklistEntity> entities) {
        throw new UnsupportedOperationException("Use stream mapping for list responses");
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TaskItemResponse implements Serializable {
        private String id;
        private String title;
        private String notes;
        private boolean completed;

        public static TaskItemResponse of(TaskItemEntity entity) {
            return TaskItemResponse.builder()
                    .id(entity.getId().toString())
                    .title(entity.getTitle())
                    .notes(entity.getNotes())
                    .completed(entity.isCompleted())
                    .build();
        }
    }
}