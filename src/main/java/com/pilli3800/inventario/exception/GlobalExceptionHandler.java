package com.pilli3800.inventario.exception;

import com.pilli3800.inventario.data.dto.response.general.SingleResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public SingleResponse<String> handleBadCredentials(
            BadCredentialsException e,
            HttpServletRequest request
    ) {
        return new SingleResponse<>(
                HttpStatus.UNAUTHORIZED.value(),
                request.getRequestURI(),
                e.getMessage()
        );
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public SingleResponse<String> handleRuntimeException(
            RuntimeException e,
            HttpServletRequest request
    ) {
        return new SingleResponse<>(
                HttpStatus.CONFLICT.value(),
                request.getRequestURI(),
                e.getMessage()
        );
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public SingleResponse<List<String>> handleValidationException(
            ValidationException e,
            HttpServletRequest req) {

        return new SingleResponse<>(
                400,
                req.getRequestURI(),
                e.getErrors()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public SingleResponse<List<String>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            HttpServletRequest req
    ) {
        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();
        return new SingleResponse<>(
                400,
                req.getRequestURI(),
                errors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public SingleResponse<List<String>> handleConstraintViolationException(
            ConstraintViolationException e,
            HttpServletRequest req
    ) {
        List<String> errors = e.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();

        return new SingleResponse<>(
                400,
                req.getRequestURI(),
                errors
        );
    }
}

