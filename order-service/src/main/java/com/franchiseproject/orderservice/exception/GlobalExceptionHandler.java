package com.franchiseproject.orderservice.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import com.franchiseproject.orderservice.dto.response.ApiResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleJsonError(
            HttpMessageNotReadableException ex
    ) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", ex.getMostSpecificCause().getMessage()));
    }

    // Business exception
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        ErrorCode ec = ex.getErrorCode();
        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .statusCode(ec.getCode())
                .message(ec.getMessage())
                .build();
        return ResponseEntity.status(ec.getHttpStatus()).body(body);
    }

//    // Bean Validation (@Valid) trên @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, List<String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        LinkedHashMap::new, // giữ thứ tự field
                        Collectors.mapping(DefaultMessageSourceResolvable::getDefaultMessage, Collectors.toList())));

        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .statusCode(ErrorCode.VALIDATION_FAILED.getCode())
                .message(ErrorCode.VALIDATION_FAILED.getMessage())
                .errors(errors)
                .build();

        return ResponseEntity.badRequest().body(body);
    }
}
