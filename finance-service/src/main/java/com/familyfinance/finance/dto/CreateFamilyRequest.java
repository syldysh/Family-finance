package com.familyfinance.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateFamilyRequest {

    @NotBlank(message = "Название группы обязательно")
    @Size(max = 100, message = "Название группы не может быть длиннее 100 символов")
    private String name;
}
