package com.eskgus.nammunity.exception;

import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.enums.Fields;
import lombok.Getter;

@Getter
public class CustomValidException extends RuntimeException {
    private final String field;
    private final String rejectedValue;
    private final String defaultMessage;

    public CustomValidException(Fields field, String rejectedValue, ExceptionMessages exceptionMessage) {
        this.field = field.getKey();
        this.rejectedValue = rejectedValue;
        this.defaultMessage = exceptionMessage.getMessage();
    }
}
