package com.mailshop_dragonvu.exception;

import com.mailshop_dragonvu.dto.ApiResponse;
import com.mailshop_dragonvu.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageService messageService;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(
            BusinessException ex, WebRequest request) {
        log.error("Business exception: {} - {}", ex.getErrorCode().getCode(), ex.getMessage());
        
        // Get localized message based on current request locale
        String localizedMessage = messageService.getErrorMessage(ex.getErrorCode().getCode());
        
        // If custom message is provided and different from default, use it
        if (ex.getCustomMessage() != null && !ex.getCustomMessage().equals(ex.getErrorCode().getMessage())) {
            localizedMessage = ex.getCustomMessage();
        }
        
        ApiResponse<?> response = ApiResponse.error(
                localizedMessage,
                ex.getErrorCode().getCode()
        );
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.error("Validation error: {}", errors);

        String localizedMessage = messageService.getErrorMessage(ErrorCode.VALIDATION_ERROR.getCode());

        ApiResponse<?> response = ApiResponse.builder()
                .success(false)
                .message(localizedMessage + ": " + errors.toString())
                .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                .data(errors)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> handleBadCredentialsException(
            BadCredentialsException ex) {
        log.error("Bad credentials: {}", ex.getMessage());
        
        String localizedMessage = messageService.getErrorMessage(ErrorCode.INVALID_CREDENTIALS.getCode());
        
        ApiResponse<?> response = ApiResponse.error(
                localizedMessage,
                ErrorCode.INVALID_CREDENTIALS.getCode()
        );
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthenticationException(
            AuthenticationException ex) {
        log.error("Authentication exception: {}", ex.getMessage());
        
        String localizedMessage = messageService.getErrorMessage(ErrorCode.UNAUTHORIZED.getCode());
        
        ApiResponse<?> response = ApiResponse.error(
                localizedMessage,
                ErrorCode.UNAUTHORIZED.getCode()
        );
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(
            AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        
        String localizedMessage = messageService.getErrorMessage(ErrorCode.FORBIDDEN.getCode());
        
        ApiResponse<?> response = ApiResponse.error(
                localizedMessage,
                ErrorCode.FORBIDDEN.getCode()
        );
        
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());
        
        String localizedMessage = messageService.getErrorMessage(ErrorCode.BAD_REQUEST.getCode());
        
        ApiResponse<?> response = ApiResponse.error(
                ex.getMessage() != null ? ex.getMessage() : localizedMessage,
                ErrorCode.BAD_REQUEST.getCode()
        );
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.error("Method not allowed: {}", ex.getMessage());

        String localizedMessage = messageService.getMessage("common.method.not_allowed", new Object[]{ex.getMethod()});

        ApiResponse<?> response = ApiResponse.error(
                localizedMessage,
                ErrorCode.METHOD_NOT_ALLOWED.getCode()
        );

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error occurred: ", ex);
        
        String localizedMessage = messageService.getErrorMessage(ErrorCode.INTERNAL_SERVER_ERROR.getCode());
        
        ApiResponse<?> response = ApiResponse.error(
                localizedMessage,
                ErrorCode.INTERNAL_SERVER_ERROR.getCode()
        );
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNoResourceFoundException(
            org.springframework.web.servlet.resource.NoResourceFoundException ex) {

        log.error("Resource not found: {}", ex.getMessage());

        String localizedMessage = messageService.getMessage("common.api.not_found");

        ApiResponse<?> response = ApiResponse.error(
                localizedMessage,
                ErrorCode.RESOURCE_NOT_FOUND.getCode()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }
}
