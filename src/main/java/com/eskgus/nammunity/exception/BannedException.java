package com.eskgus.nammunity.exception;

import org.springframework.security.authentication.AccountStatusException;

// 활동 정지된 사용자가 로그인 시도 시 던지는 예외
public class BannedException extends AccountStatusException {
    public BannedException(String msg) {
        super(msg);
    }
}
