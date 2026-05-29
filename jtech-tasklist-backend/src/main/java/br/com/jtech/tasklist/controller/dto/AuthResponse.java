package br.com.jtech.tasklist.controller.dto;

import java.io.Serializable;

public record AuthResponse(
        String token,
        String refreshToken,
        String displayName,
        String email
) implements Serializable {
}