package com.eskgus.nammunity.validation;

import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.util.CustomConstraintValidatorUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.internal.engine.messageinterpolation.util.InterpolationHelper;

import java.util.regex.Matcher;

public class CustomPatternValidator implements ConstraintValidator<CustomPattern, CharSequence> {
    private java.util.regex.Pattern pattern;
    private String escapedRegexp;
    private ExceptionMessages exceptionMessage;

    @Override
    public void initialize(CustomPattern constraintAnnotation) {
        this.exceptionMessage = constraintAnnotation.exceptionMessage();

        int intFlag = CustomConstraintValidatorUtil.getIntFlag(constraintAnnotation.flags());

        String regex = constraintAnnotation.regexp();
        this.pattern = CustomConstraintValidatorUtil.compile(regex, intFlag);

        this.escapedRegexp = InterpolationHelper.escapeMessageParameter(regex);
    }

    @Override
    public boolean isValid(CharSequence charSequence, ConstraintValidatorContext constraintValidatorContext) {
        boolean isNull = charSequence == null;

        boolean doesMatch = false;
        if (!isNull) {
            if (constraintValidatorContext instanceof HibernateConstraintValidatorContext) {
                constraintValidatorContext.unwrap(HibernateConstraintValidatorContext.class)
                        .addMessageParameter("regexp", this.escapedRegexp);
            }

            Matcher m = this.pattern.matcher(charSequence);
            doesMatch = m.matches();
        }

        boolean isValid = isNull || doesMatch;
        CustomConstraintValidatorUtil.addConstraintViolation(isValid, constraintValidatorContext, exceptionMessage);

        return isValid;
    }
}
