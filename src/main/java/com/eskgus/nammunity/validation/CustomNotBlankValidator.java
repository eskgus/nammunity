package com.eskgus.nammunity.validation;

import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.util.CustomConstraintValidatorUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CustomNotBlankValidator implements ConstraintValidator<CustomNotBlank, CharSequence> {
    private ExceptionMessages exceptionMessage;

    @Override
    public void initialize(CustomNotBlank constraintAnnotation) {
        this.exceptionMessage = constraintAnnotation.exceptionMessage();
    }

    @Override
    public boolean isValid(CharSequence charSequence, ConstraintValidatorContext constraintValidatorContext) {
        boolean isValid = charSequence != null && charSequence.toString().trim().length() > 0;
        CustomConstraintValidatorUtil.addConstraintViolation(isValid, constraintValidatorContext, exceptionMessage);

        return isValid;
    }
}
