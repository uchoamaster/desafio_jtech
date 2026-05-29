package br.com.jtech.tasklist.service;

import br.com.jtech.tasklist.controller.dto.AuthLoginRequest;
import br.com.jtech.tasklist.controller.dto.AuthRegisterRequest;
import br.com.jtech.tasklist.controller.dto.RefreshTokenRequest;
import br.com.jtech.tasklist.domain.UserEntity;
import br.com.jtech.tasklist.repository.UserRepository;
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
                var request = new AuthRegisterRequest(" Angelo ", " Angelo@Tasklist.Local ", "123456");

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
        assertThat(response.token()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.email()).isEqualTo("angelo@tasklist.local");
    }

    @Test
    void loginShouldRejectInvalidPassword() {
        var user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setName("Angelo");
        user.setEmail("angelo@tasklist.local");
        user.setPasswordHash("hashed-password");

        when(userRepository.findByEmailIgnoreCase("angelo@tasklist.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        var request = new AuthLoginRequest("angelo@tasklist.local", "wrong-password");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void refreshShouldReturnNewTokensWhenRefreshTokenIsValid() {
        var user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setName("Angelo");
        user.setEmail("angelo@tasklist.local");
        user.setPasswordHash("hashed-password");

        when(jwtService.extractSubject("refresh-token")).thenReturn("angelo@tasklist.local");
        when(userRepository.findByEmailIgnoreCase("angelo@tasklist.local")).thenReturn(Optional.of(user));
        when(jwtService.isRefreshTokenValid("refresh-token", "angelo@tasklist.local")).thenReturn(true);
        when(jwtService.generateToken("angelo@tasklist.local")).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken("angelo@tasklist.local")).thenReturn("new-refresh-token");

        var response = authService.refresh(new RefreshTokenRequest("refresh-token"));

        assertThat(response.token()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.displayName()).isEqualTo("Angelo");
    }

    @Test
    void refreshShouldRejectInvalidRefreshToken() {
        var user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setName("Angelo");
        user.setEmail("angelo@tasklist.local");
        user.setPasswordHash("hashed-password");

        when(jwtService.extractSubject("refresh-token")).thenReturn("angelo@tasklist.local");
        when(userRepository.findByEmailIgnoreCase("angelo@tasklist.local")).thenReturn(Optional.of(user));
        when(jwtService.isRefreshTokenValid("refresh-token", "angelo@tasklist.local")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("refresh-token")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}