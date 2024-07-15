package com.eskgus.nammunity.validation;

import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;

@Constraint(validatedBy = CustomEmailValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(CustomEmail.List.class)
public @interface CustomEmail {
    ExceptionMessages exceptionMessage();
    String message() default "{jakarta.validation.constraints.Email.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String regexp() default ".*";
    Pattern.Flag[] flags() default {};

    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        CustomEmail[] value();
    }
}
