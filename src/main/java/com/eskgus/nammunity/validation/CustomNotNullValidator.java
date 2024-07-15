package com.eskgus.nammunity.validation;

import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.util.CustomConstraintValidatorUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CustomNotNullValidator implements ConstraintValidator<CustomNotNull, Object> {
    private ExceptionMessages exceptionMessage;

    @Override
    public void initialize(CustomNotNull constraintAnnotation) {
        this.exceptionMessage = constraintAnnotation.exceptionMessage();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        boolean isValid = object != null;
        CustomConstraintValidatorUtil.addConstraintViolation(isValid, constraintValidatorContext, exceptionMessage);

        return isValid;
    }
}
