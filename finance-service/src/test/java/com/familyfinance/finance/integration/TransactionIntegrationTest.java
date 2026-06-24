package com.familyfinance.finance.integration;

import com.familyfinance.common.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class TransactionIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    private String tokenFor(Long userId, String email) {
        return jwtUtils.generateToken(userId, email, List.of("USER"));
    }

    @Test
    void createTransaction_thenListPersonal_returnsCreatedItem() throws Exception {
        String token = tokenFor(100L, "user100@test.com");

        String createJson = """
                {"amount":15000.50,"type":"INCOME","date":"2026-06-01","categoryId":1,"description":"Зарплата за май"}
                """;

        mockMvc.perform(post("/api/transactions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.amount").value(15000.50))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.categoryName").value("Зарплата"));

        mockMvc.perform(get("/api/transactions/personal")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.description == 'Зарплата за май')]").exists())
                .andExpect(jsonPath("$[?(@.description == 'Зарплата за май')].amount").value(15000.50));
    }

    @Test
    void createTransaction_negativeAmount_returns400() throws Exception {
        String token = tokenFor(101L, "user101@test.com");

        String badJson = """
                {"amount":-100,"type":"EXPENSE","date":"2026-06-01","categoryId":6}
                """;

        mockMvc.perform(post("/api/transactions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPersonal_withoutAuth_isRejected() throws Exception {
        mockMvc.perform(get("/api/transactions/personal"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllAccessible_userWithoutFamilies_returnsPersonalOnly() throws Exception {
        String token = tokenFor(200L, "lone@test.com");

        mockMvc.perform(post("/api/transactions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":777,"type":"INCOME","date":"2026-06-02","categoryId":1}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/transactions/all")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.amount == 777)]").exists());
    }
}
