package com.eskgus.nammunity.exception;

import lombok.Getter;

@Getter
public class CustomValidException extends RuntimeException {
    private final String field;
    private final String rejectedValue;
    private final String defaultMessage;

    public CustomValidException(String field, String rejectedValue, String defaultMessage) {
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.defaultMessage = defaultMessage;
    }
}
