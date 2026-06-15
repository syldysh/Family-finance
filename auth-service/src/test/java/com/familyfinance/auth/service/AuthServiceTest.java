package com.familyfinance.auth.service;

import com.familyfinance.auth.dto.AuthResponse;
import com.familyfinance.auth.dto.LoginRequest;
import com.familyfinance.auth.dto.RegisterRequest;
import com.familyfinance.auth.entity.Role;
import com.familyfinance.auth.entity.User;
import com.familyfinance.auth.repository.UserRepository;
import com.familyfinance.common.exception.ConflictException;
import com.familyfinance.common.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encoded_password");
        testUser.setFirstName("Иван");
        testUser.setLastName("Иванов");
        testUser.setRole(Role.USER);
    }

    @Test
    void register_shouldCreateUserAndReturnToken() {
        RegisterRequest request = new RegisterRequest("new@example.com", "password123", "Петр", "Петров");
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });
        when(jwtUtils.generateToken(eq(2L), eq("new@example.com"), any())).thenReturn("jwt_token");

        AuthResponse response = authService.register(request);

        assertEquals("jwt_token", response.getToken());
        assertEquals("new@example.com", response.getEmail());
        assertEquals("USER", response.getRole());
    }

    @Test
    void register_shouldThrowConflictIfEmailExists() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password", "А", "Б");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
    }

    @Test
    void login_shouldReturnTokenForValidCredentials() {
        LoginRequest request = new LoginRequest("test@example.com", "password");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encoded_password")).thenReturn(true);
        when(jwtUtils.generateToken(eq(1L), eq("test@example.com"), eq(List.of("USER")))).thenReturn("token");

        AuthResponse response = authService.login(request);

        assertEquals("token", response.getToken());
    }

    @Test
    void login_shouldThrowForWrongPassword() {
        LoginRequest request = new LoginRequest("test@example.com", "wrong");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong", "encoded_password")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void login_shouldThrowForNonExistentEmail() {
        LoginRequest request = new LoginRequest("no@example.com", "password");
        when(userRepository.findByEmail("no@example.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }
}
