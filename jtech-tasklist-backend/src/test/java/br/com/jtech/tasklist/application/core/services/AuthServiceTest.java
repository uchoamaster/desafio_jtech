package br.com.jtech.tasklist.application.core.services;

import br.com.jtech.tasklist.adapters.input.protocols.AuthLoginRequest;
import br.com.jtech.tasklist.adapters.input.protocols.AuthRegisterRequest;
import br.com.jtech.tasklist.adapters.input.protocols.RefreshTokenRequest;
import br.com.jtech.tasklist.adapters.output.repositories.UserRepository;
import br.com.jtech.tasklist.adapters.output.repositories.entities.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerShouldHashPasswordAndReturnAccessAndRefreshTokens() {
        var request = AuthRegisterRequest.builder()
                .name(" Angelo ")
                .email(" Angelo@Tasklist.Local ")
                .password("123456")
                .build();

        when(userRepository.existsByEmailIgnoreCase("angelo@tasklist.local")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("hashed-password");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            var entity = invocation.getArgument(0, UserEntity.class);
            entity.setId(UUID.randomUUID());
            return entity;
        });
        when(jwtService.generateToken("angelo@tasklist.local")).thenReturn("access-token");
        when(jwtService.generateRefreshToken("angelo@tasklist.local")).thenReturn("refresh-token");

        var response = authService.register(request);

        var savedUser = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(savedUser.capture());

        assertThat(savedUser.getValue().getName()).isEqualTo("Angelo");
        assertThat(savedUser.getValue().getEmail()).isEqualTo("angelo@tasklist.local");
        assertThat(savedUser.getValue().getPasswordHash()).isEqualTo("hashed-password");
        assertThat(response.getToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getEmail()).isEqualTo("angelo@tasklist.local");
    }

    @Test
    void loginShouldRejectInvalidPassword() {
        var user = UserEntity.builder()
                .id(UUID.randomUUID())
                .name("Angelo")
                .email("angelo@tasklist.local")
                .passwordHash("hashed-password")
                .build();

        when(userRepository.findByEmailIgnoreCase("angelo@tasklist.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        var request = AuthLoginRequest.builder()
                .email("angelo@tasklist.local")
                .password("wrong-password")
                .build();

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void refreshShouldReturnNewTokensWhenRefreshTokenIsValid() {
        var user = UserEntity.builder()
                .id(UUID.randomUUID())
                .name("Angelo")
                .email("angelo@tasklist.local")
                .passwordHash("hashed-password")
                .build();

        when(jwtService.extractSubject("refresh-token")).thenReturn("angelo@tasklist.local");
        when(userRepository.findByEmailIgnoreCase("angelo@tasklist.local")).thenReturn(Optional.of(user));
        when(jwtService.isRefreshTokenValid("refresh-token", "angelo@tasklist.local")).thenReturn(true);
        when(jwtService.generateToken("angelo@tasklist.local")).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken("angelo@tasklist.local")).thenReturn("new-refresh-token");

        var response = authService.refresh(RefreshTokenRequest.builder()
                .refreshToken("refresh-token")
                .build());

        assertThat(response.getToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.getDisplayName()).isEqualTo("Angelo");
    }

    @Test
    void refreshShouldRejectInvalidRefreshToken() {
        var user = UserEntity.builder()
                .id(UUID.randomUUID())
                .name("Angelo")
                .email("angelo@tasklist.local")
                .passwordHash("hashed-password")
                .build();

        when(jwtService.extractSubject("refresh-token")).thenReturn("angelo@tasklist.local");
        when(userRepository.findByEmailIgnoreCase("angelo@tasklist.local")).thenReturn(Optional.of(user));
        when(jwtService.isRefreshTokenValid("refresh-token", "angelo@tasklist.local")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(RefreshTokenRequest.builder()
                .refreshToken("refresh-token")
                .build()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}