package com.familyfinance.finance.controller;

import com.familyfinance.common.security.UserPrincipal;
import com.familyfinance.finance.dto.CreateTransactionRequest;
import com.familyfinance.finance.dto.TransactionResponse;
import com.familyfinance.finance.entity.OperationType;
import com.familyfinance.finance.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Операции", description = "Управление доходами и расходами")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @Operation(summary = "Создать операцию")
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        TransactionResponse response = transactionService.createTransaction(request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/personal")
    @Operation(summary = "Получить личные операции")
    public ResponseEntity<List<TransactionResponse>> getPersonalTransactions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) OperationType type,
            @RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(transactionService.getPersonalTransactions(
                principal.getUserId(), from, to, type, categoryId));
    }

    @GetMapping("/family/{familyId}")
    @Operation(summary = "Получить операции группы")
    public ResponseEntity<List<TransactionResponse>> getFamilyTransactions(
            @PathVariable Long familyId,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) OperationType type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(transactionService.getFamilyTransactions(
                familyId, principal.getUserId(), from, to, type, categoryId, userId));
    }

    @GetMapping("/all")
    @Operation(summary = "Получить все доступные операции (личные и из всех групп пользователя)")
    public ResponseEntity<List<TransactionResponse>> getAllAccessible(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(transactionService.getAllForUser(principal.getUserId(), from, to));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить операцию по ID")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable Long id,
                                                              @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(transactionService.getTransactionById(id, principal.getUserId()));
    }
}
