package com.eskgus.nammunity.advice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

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
        }
        return error;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public RedirectView ConfirmTokenExceptionHandler(IllegalArgumentException ex, RedirectAttributes ra) {
        ra.addFlashAttribute("error", ex.getMessage());
        return new RedirectView("/users/confirm-email");
    }
}
