package br.com.jtech.tasklist.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class TaskCreateRequest implements Serializable {

    @NotBlank(message = "Task list id is required")
    private String tasklistId;

    @NotBlank(message = "Task title is required")
    @Size(max = 120, message = "Task title must have at most 120 characters")
    private String title;

    @Size(max = 500, message = "Task notes must have at most 500 characters")
    private String notes;
}