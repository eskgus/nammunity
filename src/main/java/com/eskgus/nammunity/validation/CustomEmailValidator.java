package com.eskgus.nammunity.validation;

import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.util.CustomConstraintValidatorUtil;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.internal.constraintvalidators.AbstractEmailValidator;

import java.util.regex.Matcher;

public class CustomEmailValidator extends AbstractEmailValidator<CustomEmail> {
    private java.util.regex.Pattern pattern;
    private ExceptionMessages exceptionMessage;

    public void initialize(CustomEmail constraintAnnotation) {
        this.exceptionMessage = constraintAnnotation.exceptionMessage();

        Pattern.Flag[] flags = constraintAnnotation.flags();
        int intFlag = CustomConstraintValidatorUtil.getIntFlag(flags);

        String regex = constraintAnnotation.regexp();
        if (!".*".equals(regex) || flags.length > 0) {
            this.pattern = CustomConstraintValidatorUtil.compile(regex, intFlag);
        }
    }

    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        boolean isNull = value == null;

        boolean isSuperValid = false;
        boolean doesMatch = false;
        if (!isNull) {
            isSuperValid = super.isValid(value, context);

            if (this.pattern != null && isSuperValid) {
                Matcher m = this.pattern.matcher(value);
                doesMatch = m.matches();
            }
        }

        boolean isValid = isNull || (isSuperValid || doesMatch);
        CustomConstraintValidatorUtil.addConstraintViolation(isValid, context, exceptionMessage);

        return isValid;
    }
}
