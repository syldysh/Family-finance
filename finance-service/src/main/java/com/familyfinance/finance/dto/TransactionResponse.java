package com.familyfinance.finance.dto;

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
public class TransactionResponse {

    private Long id;
    private BigDecimal amount;
    private String type;
    private String description;
    private LocalDate date;
    private Long userId;
    private String categoryName;
    private Long categoryId;
    private Long familyId;
    private String familyName;
}
