package franchiseproject.inventory_service.exception;

import franchiseproject.inventory_service.dto.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //    @ExceptionHandler(value = Exception.class)
//    public ResponseEntity<ApiResponse> handlingRuntimeException(Exception exception) {
//        ApiResponse response = ApiResponse.builder()
//                .statusCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
//                .message(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage())
//                .build();
//        return ResponseEntity.badRequest().body(response);
//    }

    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();;
        ApiResponse response = ApiResponse.builder()
                .statusCode(errorCode.getCode())
                .message(exception.getMessage())
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    // bat exception vi pham validation
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handlingMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String errorKey = exception.getFieldError().getDefaultMessage();
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        try {
            errorCode = ErrorCode.valueOf(errorKey);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
        ApiResponse response = ApiResponse.builder()
                .statusCode(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    public ResponseEntity<ApiResponse> handlingNoResourceFoundException (NoResourceFoundException exception) {
        ErrorCode errorCode = ErrorCode.NOT_FOUND;
        return ResponseEntity.badRequest()
                .body(ApiResponse.builder()
                        .statusCode(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handlingDataIntegrityViolationException (DataIntegrityViolationException exception) {
        ErrorCode errorCode = ErrorCode.DUPLICATE_KEY;
        return ResponseEntity.badRequest()
                .body(ApiResponse.builder()
                        .statusCode(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }
    // 404 - Resource not found
//    @ExceptionHandler(ResourceNotFoundException.class)
//    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
//        return build(HttpStatus.NOT_FOUND, ex.getMessage());
//    }
//
//    // 400 - Bad request custom
//    @ExceptionHandler(BadRequestException.class)
//    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
//        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
//    }
//
//    // 400 - UUID format error
//    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
//    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
//
//        if (ex.getRequiredType() == UUID.class) {
//            return build(HttpStatus.BAD_REQUEST, "Invalid UUID format");
//        }
//
//        return build(HttpStatus.BAD_REQUEST, "Invalid request parameter");
//    }
//
//    // 400 - Validation error (@NotBlank, @NotNull...)
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
//
//        String message = ex.getBindingResult()
//                .getFieldError()
//                .getDefaultMessage();
//
//        return build(HttpStatus.BAD_REQUEST, message);
//    }
//
//    // 500 - Other errors
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Map<String, Object>> handleOtherExceptions(Exception ex) {
//        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
//    }
//
//    // Common response builder
//    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("timestamp", Instant.now());
//        response.put("status", status.value());
//        response.put("message", message);
//
//        return new ResponseEntity<>(response, status);
//    }
}