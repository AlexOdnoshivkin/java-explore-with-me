package ru.practicum.ewmservice.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class AfterTimeValidator implements ConstraintValidator<AfterTime, LocalDateTime> {

    @Override
    public void initialize(AfterTime constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }


    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return LocalDateTime.now().plusHours(2).isBefore(value);
    }

}
