package br.com.jtech.tasklist.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record AuthRegisterRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 80, message = "Name must have at most 80 characters")
        String name,
        @Email(message = "Provide a valid email")
        @NotBlank(message = "Email is required")
        String email,
        @NotBlank(message = "Password is required")
        @Size(min = 4, max = 120, message = "Password must have between 4 and 120 characters")
        String password
) implements Serializable {
}