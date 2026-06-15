package com.familyfinance.finance.service;

import com.familyfinance.common.exception.NotFoundException;
import com.familyfinance.finance.dto.CategoryResponse;
import com.familyfinance.finance.dto.CreateCategoryRequest;
import com.familyfinance.finance.entity.Category;
import com.familyfinance.finance.entity.OperationType;
import com.familyfinance.finance.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request, Long userId) {
        Category category = new Category();
        category.setName(request.getName());
        category.setType(request.getType());
        category.setUserId(userId);
        categoryRepository.save(category);
        return toResponse(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(Long userId, OperationType type) {
        List<Category> categories;
        if (type != null) {
            categories = categoryRepository.findAvailableForUserByType(userId, type);
        } else {
            categories = categoryRepository.findAvailableForUser(userId);
        }
        return categories.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getType().name());
    }
}
