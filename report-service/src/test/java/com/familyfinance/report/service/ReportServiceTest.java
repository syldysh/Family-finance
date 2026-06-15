package com.familyfinance.report.service;

import com.familyfinance.report.client.FinanceServiceClient;
import com.familyfinance.report.dto.ReportRequest;
import com.familyfinance.report.dto.ReportResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private FinanceServiceClient financeClient;

    @InjectMocks
    private ReportService reportService;

    @Test
    void buildPersonalReport_shouldCalculateTotals() {
        ReportRequest request = new ReportRequest();
        request.setDateFrom(LocalDate.of(2024, 1, 1));
        request.setDateTo(LocalDate.of(2024, 1, 31));

        List<Map<String, Object>> transactions = List.of(
                Map.of("amount", 50000, "type", "INCOME", "categoryName", "Зарплата", "userId", 1),
                Map.of("amount", 5000, "type", "EXPENSE", "categoryName", "Продукты", "userId", 1),
                Map.of("amount", 3000, "type", "EXPENSE", "categoryName", "Транспорт", "userId", 1)
        );

        when(financeClient.getPersonalTransactions(1L, request.getDateFrom(), request.getDateTo()))
                .thenReturn(transactions);

        ReportResponse report = reportService.buildPersonalReport(1L, request);

        assertEquals(new BigDecimal("50000"), report.getTotalIncome());
        assertEquals(new BigDecimal("8000"), report.getTotalExpense());
        assertEquals(new BigDecimal("42000"), report.getBalance());
        assertEquals(3, report.getTransactionCount());
        assertEquals(new BigDecimal("50000"), report.getIncomeByCategory().get("Зарплата"));
        assertEquals(new BigDecimal("5000"), report.getExpenseByCategory().get("Продукты"));
    }

    @Test
    void buildPersonalReport_shouldThrowForInvalidPeriod() {
        ReportRequest request = new ReportRequest();
        request.setDateFrom(LocalDate.of(2024, 2, 1));
        request.setDateTo(LocalDate.of(2024, 1, 1));

        assertThrows(IllegalArgumentException.class, () -> reportService.buildPersonalReport(1L, request));
    }

    @Test
    void buildPersonalReport_shouldHandleEmptyTransactions() {
        ReportRequest request = new ReportRequest();
        request.setDateFrom(LocalDate.of(2024, 1, 1));
        request.setDateTo(LocalDate.of(2024, 1, 31));

        when(financeClient.getPersonalTransactions(1L, request.getDateFrom(), request.getDateTo()))
                .thenReturn(List.of());

        ReportResponse report = reportService.buildPersonalReport(1L, request);

        assertEquals(BigDecimal.ZERO, report.getTotalIncome());
        assertEquals(BigDecimal.ZERO, report.getTotalExpense());
        assertEquals(0, report.getTransactionCount());
    }
}
