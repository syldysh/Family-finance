package com.familyfinance.finance.dto;

import com.familyfinance.finance.entity.OperationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 100, message = "Название категории не может быть длиннее 100 символов")
    private String name;

    @NotNull(message = "Тип категории обязателен")
    private OperationType type;
}
