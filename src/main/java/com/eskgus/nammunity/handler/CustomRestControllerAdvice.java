package com.eskgus.nammunity.handler;

import com.eskgus.nammunity.exception.CustomValidException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@RestControllerAdvice(basePackages = "com.eskgus.nammunity.web.controller.api")
public class CustomRestControllerAdvice {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<FieldError>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = sortFieldErrors(ex.getBindingResult().getFieldErrors());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().iterator().next().getMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    @ExceptionHandler(CustomValidException.class)
    public ResponseEntity<List<CustomValidException>> handleCustomValidException(CustomValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonList(ex));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleOtherExceptions(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("서버 오류가 발생했습니다. 관리자에게 문의하세요.");
    }

    private List<FieldError> sortFieldErrors(List<FieldError> fieldErrors) {
        String notBlank = "CustomNotBlank";
        boolean hasNotBlankError = fieldErrors.stream().anyMatch(error -> notBlank.equals(error.getCode()));

        if (hasNotBlankError && !notBlank.equals(fieldErrors.get(0).getCode())) {
            return fieldErrors.stream()
                    .sorted(Comparator.comparingInt(error -> notBlank.equals(error.getCode()) ? -1 : 0))
                    .toList();
        }

        return fieldErrors;
    }
}
