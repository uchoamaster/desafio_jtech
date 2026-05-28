package br.com.jtech.tasklist.application.core.services;

import br.com.jtech.tasklist.adapters.input.protocols.AuthLoginRequest;
import br.com.jtech.tasklist.adapters.input.protocols.AuthRegisterRequest;
import br.com.jtech.tasklist.adapters.input.protocols.AuthResponse;
import br.com.jtech.tasklist.adapters.input.protocols.RefreshTokenRequest;
import br.com.jtech.tasklist.adapters.output.repositories.UserRepository;
import br.com.jtech.tasklist.adapters.output.repositories.entities.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(AuthRegisterRequest request) {
        var normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered.");
        }

        var user = userRepository.save(UserEntity.builder()
                .name(request.getName().trim())
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.getPassword().trim()))
                .build());

        return createResponse(user);
    }

    public AuthResponse login(AuthLoginRequest request) {
        var normalizedEmail = request.getEmail().trim().toLowerCase();
        var user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials."));

        if (!passwordEncoder.matches(request.getPassword().trim(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials.");
        }

        return createResponse(user);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        var token = request.getRefreshToken().trim();
        var subject = jwtService.extractSubject(token);

        var user = userRepository.findByEmailIgnoreCase(subject)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token."));

        if (!jwtService.isRefreshTokenValid(token, user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token.");
        }

        return createResponse(user);
    }

    private AuthResponse createResponse(UserEntity user) {
        return AuthResponse.builder()
                .token(jwtService.generateToken(user.getEmail()))
                .refreshToken(jwtService.generateRefreshToken(user.getEmail()))
                .displayName(user.getName())
                .email(user.getEmail())
                .build();
    }
}