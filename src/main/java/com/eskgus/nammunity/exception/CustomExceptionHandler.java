package com.eskgus.nammunity.exception;

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
    public Map<String, String> ExistingValueExceptionHandler(ResponseStatusException ex) {
        Map<String, String> error = new HashMap<>();
        if (ex.getReason().equals("username")) {
            error.put("existingUsername", "이미 사용 중인 ID입니다.");
        } else if (ex.getReason().equals("nickname")) {
            error.put("existingNickname", "이미 사용 중인 닉네임입니다.");
        }
        return error;
    }
}
