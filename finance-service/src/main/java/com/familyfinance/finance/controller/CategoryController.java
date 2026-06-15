package com.familyfinance.finance.controller;

import com.familyfinance.common.security.UserPrincipal;
import com.familyfinance.finance.dto.CategoryResponse;
import com.familyfinance.finance.dto.CreateCategoryRequest;
import com.familyfinance.finance.entity.OperationType;
import com.familyfinance.finance.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Категории", description = "Управление категориями доходов и расходов")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @Operation(summary = "Создать категорию")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request,
                                                           @AuthenticationPrincipal UserPrincipal principal) {
        CategoryResponse response = categoryService.createCategory(request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Получить список категорий")
    public ResponseEntity<List<CategoryResponse>> getCategories(@AuthenticationPrincipal UserPrincipal principal,
                                                                @RequestParam(required = false) OperationType type) {
        return ResponseEntity.ok(categoryService.getCategories(principal.getUserId(), type));
    }
}
