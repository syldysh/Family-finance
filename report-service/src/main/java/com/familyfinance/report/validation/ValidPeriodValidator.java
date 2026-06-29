package com.familyfinance.report.validation;

import com.familyfinance.report.dto.ReportRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidPeriodValidator implements ConstraintValidator<ValidPeriod, ReportRequest> {

    @Override
    public boolean isValid(ReportRequest request, ConstraintValidatorContext context) {
        if (request.getDateFrom() == null || request.getDateTo() == null) {
            return true;
        }
        return !request.getDateFrom().isAfter(request.getDateTo());
    }
}
