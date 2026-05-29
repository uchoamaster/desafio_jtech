package br.com.jtech.tasklist.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public record AuthLoginRequest(
        @Email(message = "Provide a valid email")
        @NotBlank(message = "Email is required")
        String email,
        @NotBlank(message = "Password is required")
        String password
) implements Serializable {
}