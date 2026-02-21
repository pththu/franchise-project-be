package com.franchiseproject.loyaltyservice.exception;

import com.franchiseproject.loyaltyservice.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse<Void>> handlingRuntimeException(Exception exception) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .statusCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
                .message(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage())
                .build();
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse<Void>> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .statusCode(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        int httpStatus = (String.valueOf(errorCode.getCode()).startsWith("404")) ? 404 : 400;
        return ResponseEntity.status(httpStatus).body(response);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handlingMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String errorKey = exception.getFieldError().getDefaultMessage();
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        try {
            errorCode = ErrorCode.valueOf(errorKey);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .statusCode(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handlingNoResourceFoundException (NoResourceFoundException exception) {
        ErrorCode errorCode = ErrorCode.NOT_FOUND;
        return ResponseEntity.status(404)
                .body(ApiResponse.<Void>builder()
                        .statusCode(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }
}