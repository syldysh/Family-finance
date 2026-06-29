package com.familyfinance.report.dto;

import com.familyfinance.report.validation.ValidPeriod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidPeriod
public class ReportRequest {

    @NotNull(message = "Дата начала периода обязательна")
    private LocalDate dateFrom;

    @NotNull(message = "Дата конца периода обязательна")
    private LocalDate dateTo;

    private Long familyId;

    private List<Long> familyIds;
}
