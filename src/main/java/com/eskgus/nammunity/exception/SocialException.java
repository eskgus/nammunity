package com.eskgus.nammunity.exception;

import com.eskgus.nammunity.domain.enums.Fields;
import com.eskgus.nammunity.domain.enums.SocialType;
import lombok.Getter;

import java.util.Optional;

@Getter
public class SocialException extends RuntimeException {
    private final String username;
    private final String field;
    private final String rejectedValue;

    public SocialException(String username, Fields field, SocialType rejectedValue) {
        super(String.format("username: %s, 유효하지 않은 값 %s: %s",
                username,
                field.getKey(),
                Optional.ofNullable(rejectedValue).map(SocialType::getKey).orElse("null")));
        this.username = username;
        this.field = field.getKey();
        this.rejectedValue = rejectedValue != null ? rejectedValue.getKey() : null;
    }
}
