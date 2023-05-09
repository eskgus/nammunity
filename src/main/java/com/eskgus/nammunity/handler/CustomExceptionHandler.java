package com.eskgus.nammunity.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class CustomExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> PatternExceptionHandler(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                    .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return errors;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ResponseStatusException.class)
    public Map<String, String> SignUpExceptionHandler(ResponseStatusException ex) {
        Map<String, String> error = new HashMap<>();
        String reason = ex.getReason();
        switch (reason) {
            case "username":
                error.put(reason, "이미 사용 중인 ID입니다.");
                break;
            case "confirmPassword":
                error.put(reason, "비밀번호가 일치하지 않습니다.");
                break;
            case "nickname":
                error.put(reason, "이미 사용 중인 닉네임입니다.");
                break;
            case "email":
                error.put(reason, "이미 사용 중인 이메일입니다.");
                break;
            case "confirmEmail":
                error.put(reason, "인증되지 않은 이메일입니다.");
                break;
            case "resendToken":
                error.put(reason, "더 이상 재발송할 수 없어요. 다시 가입해 주세요.");
                break;
        }
        return error;
    }
}
