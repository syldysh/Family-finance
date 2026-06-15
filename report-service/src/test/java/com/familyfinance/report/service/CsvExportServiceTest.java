package com.familyfinance.report.service;

import com.familyfinance.report.dto.ReportResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvExportServiceTest {

    private final CsvExportService csvExportService = new CsvExportService();

    @Test
    void exportReport_shouldContainHeaders() {
        ReportResponse report = ReportResponse.builder()
                .dateFrom(LocalDate.of(2024, 1, 1))
                .dateTo(LocalDate.of(2024, 1, 31))
                .totalIncome(new BigDecimal("50000"))
                .totalExpense(new BigDecimal("8000"))
                .balance(new BigDecimal("42000"))
                .incomeByCategory(Map.of("Зарплата", new BigDecimal("50000")))
                .expenseByCategory(Map.of("Продукты", new BigDecimal("8000")))
                .byUser(List.of(ReportResponse.UserSummary.builder()
                        .userId(1L)
                        .income(new BigDecimal("50000"))
                        .expense(new BigDecimal("8000"))
                        .balance(new BigDecimal("42000"))
                        .build()))
                .transactionCount(3)
                .build();

        byte[] csv = csvExportService.exportReport(report);
        String content = new String(csv);

        assertTrue(content.contains("Общая сводка"));
        assertTrue(content.contains("50000"));
        assertTrue(content.contains("Зарплата"));
        assertTrue(content.contains("Продукты"));
    }
}
