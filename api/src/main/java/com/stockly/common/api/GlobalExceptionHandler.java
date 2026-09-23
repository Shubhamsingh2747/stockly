package com.stockly.common.api;

import com.stockly.common.exception.BusinessException;
import com.stockly.common.exception.ConflictException;
import com.stockly.common.exception.NotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> notFound(NotFoundException ex) {
        return respond(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> conflict(ConflictException ex) {
        return respond(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> business(BusinessException ex) {
        return respond(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> badCredentials(BadCredentialsException ex) {
        return respond(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .orElse("Validation failed");
        return respond(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> constraint(ConstraintViolationException ex) {
        return respond(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> optimisticLock(ObjectOptimisticLockingFailureException ex) {
        return respond(HttpStatus.CONFLICT, "Stock was updated by another request. Retry the operation.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integrity(DataIntegrityViolationException ex) {
        return respond(HttpStatus.CONFLICT, "A unique constraint was violated");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> fallback(Exception ex) {
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
    }

    private ResponseEntity<ApiError> respond(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ApiError.of(status.value(), message));
    }
}
