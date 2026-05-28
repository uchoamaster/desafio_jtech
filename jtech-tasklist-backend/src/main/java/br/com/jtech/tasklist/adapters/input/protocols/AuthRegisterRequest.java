package br.com.jtech.tasklist.adapters.input.protocols;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class AuthRegisterRequest implements Serializable {

    @NotBlank(message = "Name is required")
    @Size(max = 80, message = "Name must have at most 80 characters")
    private String name;

    @Email(message = "Provide a valid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 4, max = 120, message = "Password must have between 4 and 120 characters")
    private String password;
}