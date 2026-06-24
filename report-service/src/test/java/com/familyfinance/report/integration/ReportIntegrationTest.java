package com.familyfinance.report.integration;

import com.familyfinance.common.security.JwtUtils;
import com.familyfinance.report.client.FinanceServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @MockBean
    private FinanceServiceClient financeClient;

    private String tokenFor(Long userId, String email) {
        return jwtUtils.generateToken(userId, email, List.of("USER"));
    }

    @Test
    void personalReport_returnsAggregatedTotals() throws Exception {
        String token = tokenFor(42L, "u42@test.com");

        when(financeClient.getPersonalTransactions(eq(42L), any(), any()))
                .thenReturn(List.of(
                        Map.of("amount", 30000, "type", "INCOME", "categoryName", "Зарплата", "userId", 42),
                        Map.of("amount", 4000, "type", "EXPENSE", "categoryName", "Продукты", "userId", 42),
                        Map.of("amount", 2000, "type", "EXPENSE", "categoryName", "Транспорт", "userId", 42)
                ));

        mockMvc.perform(post("/api/reports/personal")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dateFrom":"2026-01-01","dateTo":"2026-01-31"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(30000))
                .andExpect(jsonPath("$.totalExpense").value(6000))
                .andExpect(jsonPath("$.balance").value(24000))
                .andExpect(jsonPath("$.transactionCount").value(3));
    }

    @Test
    void personalReport_withoutAuth_isRejected() throws Exception {
        mockMvc.perform(post("/api/reports/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dateFrom":"2026-01-01","dateTo":"2026-01-31"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void exportPersonalReport_returnsCsvWithAttachment() throws Exception {
        String token = tokenFor(50L, "u50@test.com");

        when(financeClient.getPersonalTransactions(eq(50L), any(), any()))
                .thenReturn(List.of(
                        Map.of("amount", 1000, "type", "INCOME", "categoryName", "Зарплата", "userId", 50)
                ));

        mockMvc.perform(post("/api/reports/personal/export")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dateFrom":"2026-01-01","dateTo":"2026-01-31"}
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(content().contentTypeCompatibleWith("text/csv"));
    }
}
