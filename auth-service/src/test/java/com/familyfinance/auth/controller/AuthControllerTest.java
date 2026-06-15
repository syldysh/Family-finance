package com.familyfinance.auth.controller;

import com.familyfinance.auth.config.SecurityConfig;
import com.familyfinance.auth.dto.AuthResponse;
import com.familyfinance.auth.exception.AuthExceptionHandler;
import com.familyfinance.auth.service.AuthService;
import com.familyfinance.common.exception.ConflictException;
import com.familyfinance.common.exception.GlobalExceptionHandler;
import com.familyfinance.common.security.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, AuthExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    void register_shouldReturn201() throws Exception {
        when(authService.register(any())).thenReturn(new AuthResponse("token", "a@b.com", "USER"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email":"a@b.com","password":"123456","firstName":"A","lastName":"B"}
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token"));
    }

    @Test
    void register_shouldReturn400ForInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email":"invalid","password":"123456","firstName":"A","lastName":"B"}
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn409ForDuplicateEmail() throws Exception {
        when(authService.register(any())).thenThrow(new ConflictException("Пользователь уже существует"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email":"a@b.com","password":"123456","firstName":"A","lastName":"B"}
                            """))
                .andExpect(status().isConflict());
    }

    @Test
    void login_shouldReturn200() throws Exception {
        when(authService.login(any())).thenReturn(new AuthResponse("token", "a@b.com", "USER"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email":"a@b.com","password":"123456"}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"));
    }

    @Test
    void login_shouldReturn401ForBadCredentials() throws Exception {
        when(authService.login(any())).thenThrow(new BadCredentialsException("Неверный email или пароль"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email":"a@b.com","password":"wrong"}
                            """))
                .andExpect(status().isUnauthorized());
    }
}
