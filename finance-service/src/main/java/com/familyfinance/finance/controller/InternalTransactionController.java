package com.familyfinance.finance.controller;

import com.familyfinance.finance.dto.TransactionResponse;
import com.familyfinance.finance.entity.OperationType;
import com.familyfinance.finance.service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/internal/transactions")
public class InternalTransactionController {

    private final TransactionService transactionService;

    public InternalTransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/personal")
    public ResponseEntity<List<TransactionResponse>> getPersonalTransactions(
            @RequestParam Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) OperationType type,
            @RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(transactionService.getPersonalTransactions(userId, from, to, type, categoryId));
    }

    @GetMapping("/family/{familyId}")
    public ResponseEntity<List<TransactionResponse>> getFamilyTransactions(
            @PathVariable Long familyId,
            @RequestParam Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) OperationType type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long filterUserId) {
        return ResponseEntity.ok(transactionService.getFamilyTransactions(
                familyId, userId, from, to, type, categoryId, filterUserId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<TransactionResponse>> getAllAccessible(
            @RequestParam Long userId,
            @RequestParam List<Long> familyIds,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(transactionService.getAllAccessibleTransactions(userId, familyIds, from, to));
    }
}
