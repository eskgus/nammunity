package com.eskgus.nammunity.util;

import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.regex.PatternSyntaxException;

public class CustomConstraintValidatorUtil {
    public static int getIntFlag(Pattern.Flag[] flags) {
        int intFlag = 0;
        for (Pattern.Flag flag : flags) {
            intFlag |= flag.getValue();
        }

        return intFlag;
    }

    public static java.util.regex.Pattern compile(String regex, int intFlag) {
        try {
            return java.util.regex.Pattern.compile(regex, intFlag);
        } catch (PatternSyntaxException ex) {
            throw LoggerFactory.make(MethodHandles.lookup()).getInvalidRegularExpressionException(ex);
        }
    }

    public static void addConstraintViolation(boolean isValid, ConstraintValidatorContext constraintValidatorContext,
                                              ExceptionMessages exceptionMessage) {
        if (!isValid) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(exceptionMessage.getMessage())
                    .addConstraintViolation();
        }
    }
}
