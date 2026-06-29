package com.familyfinance.finance.dto;

import com.familyfinance.finance.entity.OperationType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {

    @NotNull(message = "Сумма обязательна")
    @Positive(message = "Сумма должна быть больше нуля")
    private BigDecimal amount;

    @NotNull(message = "Тип операции обязателен")
    private OperationType type;

    @Size(max = 255, message = "Описание не может быть длиннее 255 символов")
    private String description;

    @NotNull(message = "Дата обязательна")
    @PastOrPresent(message = "Дата не может быть в будущем")
    private LocalDate date;

    @NotNull(message = "Категория обязательна")
    private Long categoryId;

    private Long familyId;
}
