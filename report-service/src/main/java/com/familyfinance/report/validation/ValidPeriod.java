package com.familyfinance.report.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidPeriodValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPeriod {
    String message() default "Дата начала периода не может быть позже даты конца";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
