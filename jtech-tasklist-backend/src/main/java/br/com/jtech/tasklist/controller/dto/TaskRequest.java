package br.com.jtech.tasklist.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskRequest(
        @NotBlank(message = "Task title is required")
        @Size(max = 120, message = "Task title must have at most 120 characters")
        String title,
        @Size(max = 500, message = "Task notes must have at most 500 characters")
        String notes
) implements Serializable {
}