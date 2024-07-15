package com.eskgus.nammunity.validation;

import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.util.CustomConstraintValidatorUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class CustomSizeValidator implements ConstraintValidator<CustomSize, CharSequence> {
    private static final Log LOG = LoggerFactory.make(MethodHandles.lookup());
    private int min;
    private int max;
    private ExceptionMessages exceptionMessage;

    @Override
    public void initialize(CustomSize constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
        this.exceptionMessage = constraintAnnotation.exceptionMessage();
        this.validateParameters();
    }

    @Override
    public boolean isValid(CharSequence charSequence, ConstraintValidatorContext constraintValidatorContext) {
        boolean isNull = charSequence == null;

        boolean isLengthValid = false;
        if (!isNull) {
            int length = charSequence.length();
            isLengthValid = length >= this.min && length <= this.max;
        }

        boolean isValid = isNull || isLengthValid;
        CustomConstraintValidatorUtil.addConstraintViolation(isValid, constraintValidatorContext, exceptionMessage);

        return isValid;
    }

    private void validateParameters() {
        if (this.min < 0) {
            throw LOG.getMinCannotBeNegativeException();
        } else if (this.max < 0) {
            throw LOG.getMaxCannotBeNegativeException();
        } else if (this.max < this.min) {
            throw LOG.getLengthCannotBeNegativeException();
        }
    }
}
