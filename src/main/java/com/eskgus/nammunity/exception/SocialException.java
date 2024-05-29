package com.eskgus.nammunity.exception;

import lombok.Getter;

@Getter
public class SocialException extends RuntimeException {
    private final String username;
    private final String field;
    private final String rejectedValue;

    public SocialException(String username, String field, String rejectedValue) {
        super(String.format("username: %s, 유효하지 않은 값 %s: %s", username, field, rejectedValue));
        this.username = username;
        this.field = field;
        this.rejectedValue = rejectedValue;
    }
}
