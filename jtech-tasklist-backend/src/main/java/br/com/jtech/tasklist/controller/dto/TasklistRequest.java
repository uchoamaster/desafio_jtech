package br.com.jtech.tasklist.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TasklistRequest(
        @NotBlank(message = "Task list name is required")
        @Size(max = 80, message = "Task list name must have at most 80 characters")
        String name
) implements Serializable {
}