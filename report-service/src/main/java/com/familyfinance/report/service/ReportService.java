package com.familyfinance.report.service;

import com.familyfinance.report.client.FinanceServiceClient;
import com.familyfinance.report.dto.ReportRequest;
import com.familyfinance.report.dto.ReportResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final FinanceServiceClient financeClient;

    public ReportService(FinanceServiceClient financeClient) {
        this.financeClient = financeClient;
    }

    public ReportResponse buildPersonalReport(Long userId, ReportRequest request) {
        log.info("Построение личного отчёта: userId={}, period={}..{}",
                userId, request.getDateFrom(), request.getDateTo());
        validatePeriod(request);
        List<Map<String, Object>> transactions = financeClient.getPersonalTransactions(
                userId, request.getDateFrom(), request.getDateTo());
        ReportResponse response = buildReport(transactions, request);
        log.info("Личный отчёт готов: userId={}, transactions={}, balance={}",
                userId, response.getTransactionCount(), response.getBalance());
        return response;
    }

    public ReportResponse buildFamilyReport(Long userId, ReportRequest request) {
        log.info("Построение семейного отчёта: userId={}, familyId={}, period={}..{}",
                userId, request.getFamilyId(), request.getDateFrom(), request.getDateTo());
        validatePeriod(request);
        List<Map<String, Object>> transactions = financeClient.getFamilyTransactions(
                request.getFamilyId(), userId, request.getDateFrom(), request.getDateTo());
        ReportResponse response = buildReport(transactions, request);
        log.info("Семейный отчёт готов: familyId={}, transactions={}, balance={}",
                request.getFamilyId(), response.getTransactionCount(), response.getBalance());
        return response;
    }

    public ReportResponse buildFullReport(Long userId, ReportRequest request) {
        log.info("Построение полного отчёта: userId={}, familyIds={}, period={}..{}",
                userId, request.getFamilyIds(), request.getDateFrom(), request.getDateTo());
        validatePeriod(request);
        List<Long> familyIds = request.getFamilyIds() != null ? request.getFamilyIds() : List.of();
        List<Map<String, Object>> transactions = financeClient.getAllAccessible(
                userId, familyIds, request.getDateFrom(), request.getDateTo());
        ReportResponse response = buildReport(transactions, request);
        log.info("Полный отчёт готов: userId={}, transactions={}, balance={}",
                userId, response.getTransactionCount(), response.getBalance());
        return response;
    }

    private ReportResponse buildReport(List<Map<String, Object>> transactions, ReportRequest request) {
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        Map<String, BigDecimal> incomeByCategory = new TreeMap<>();
        Map<String, BigDecimal> expenseByCategory = new TreeMap<>();
        Map<Long, BigDecimal> userIncome = new HashMap<>();
        Map<Long, BigDecimal> userExpense = new HashMap<>();

        for (Map<String, Object> t : transactions) {
            BigDecimal amount = new BigDecimal(t.get("amount").toString());
            String type = (String) t.get("type");
            String category = (String) t.get("categoryName");
            Long userId = ((Number) t.get("userId")).longValue();

            if ("INCOME".equals(type)) {
                totalIncome = totalIncome.add(amount);
                incomeByCategory.merge(category, amount, BigDecimal::add);
                userIncome.merge(userId, amount, BigDecimal::add);
            } else {
                totalExpense = totalExpense.add(amount);
                expenseByCategory.merge(category, amount, BigDecimal::add);
                userExpense.merge(userId, amount, BigDecimal::add);
            }
        }

        Set<Long> allUserIds = new HashSet<>();
        allUserIds.addAll(userIncome.keySet());
        allUserIds.addAll(userExpense.keySet());

        List<ReportResponse.UserSummary> byUser = allUserIds.stream()
                .map(uid -> {
                    BigDecimal inc = userIncome.getOrDefault(uid, BigDecimal.ZERO);
                    BigDecimal exp = userExpense.getOrDefault(uid, BigDecimal.ZERO);
                    return ReportResponse.UserSummary.builder()
                            .userId(uid)
                            .income(inc)
                            .expense(exp)
                            .balance(inc.subtract(exp))
                            .build();
                })
                .collect(Collectors.toList());

        return ReportResponse.builder()
                .dateFrom(request.getDateFrom())
                .dateTo(request.getDateTo())
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(totalIncome.subtract(totalExpense))
                .incomeByCategory(incomeByCategory)
                .expenseByCategory(expenseByCategory)
                .byUser(byUser)
                .transactionCount(transactions.size())
                .build();
    }

    private void validatePeriod(ReportRequest request) {
        if (request.getDateFrom().isAfter(request.getDateTo())) {
            throw new IllegalArgumentException("Дата начала периода не может быть позже даты конца");
        }
    }
}
