package com.familyfinance.finance.dto;

import com.familyfinance.finance.entity.OperationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {

    @NotBlank(message = "Название категории обязательно")
    private String name;

    @NotNull(message = "Тип категории обязателен")
    private OperationType type;
}
