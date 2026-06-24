package com.familyfinance.report.service;

import com.familyfinance.report.dto.ReportResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class CsvExportService {

    private static final Logger log = LoggerFactory.getLogger(CsvExportService.class);

    public byte[] exportReport(ReportResponse report) {
        log.info("Экспорт отчёта в CSV: period={}..{}, transactions={}",
                report.getDateFrom(), report.getDateTo(), report.getTransactionCount());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));

        writer.println('﻿' + "Отчет за период;" + report.getDateFrom() + ";" + report.getDateTo());
        writer.println();
        writer.println("Общая сводка");
        writer.println("Доходы;" + report.getTotalIncome());
        writer.println("Расходы;" + report.getTotalExpense());
        writer.println("Баланс;" + report.getBalance());
        writer.println("Количество операций;" + report.getTransactionCount());
        writer.println();

        writer.println("Доходы по категориям");
        writer.println("Категория;Сумма");
        for (Map.Entry<String, BigDecimal> entry : report.getIncomeByCategory().entrySet()) {
            writer.println(entry.getKey() + ";" + entry.getValue());
        }
        writer.println();

        writer.println("Расходы по категориям");
        writer.println("Категория;Сумма");
        for (Map.Entry<String, BigDecimal> entry : report.getExpenseByCategory().entrySet()) {
            writer.println(entry.getKey() + ";" + entry.getValue());
        }
        writer.println();

        if (report.getByUser() != null && !report.getByUser().isEmpty()) {
            writer.println("По участникам");
            writer.println("ID пользователя;Доходы;Расходы;Баланс");
            for (ReportResponse.UserSummary u : report.getByUser()) {
                writer.println(u.getUserId() + ";" + u.getIncome() + ";" + u.getExpense() + ";" + u.getBalance());
            }
        }

        writer.flush();
        byte[] result = out.toByteArray();
        log.info("CSV отчёт сформирован: {} байт", result.length);
        return result;
    }
}
