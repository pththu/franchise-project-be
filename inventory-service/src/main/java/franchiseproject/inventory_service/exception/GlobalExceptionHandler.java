package franchiseproject.inventory_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 - Resource not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 400 - Bad request custom
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 400 - UUID format error
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {

        if (ex.getRequiredType() == UUID.class) {
            return build(HttpStatus.BAD_REQUEST, "Invalid UUID format");
        }

        return build(HttpStatus.BAD_REQUEST, "Invalid request parameter");
    }

    // 400 - Validation error (@NotBlank, @NotNull...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();

        return build(HttpStatus.BAD_REQUEST, message);
    }

    // 500 - Other errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOtherExceptions(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    // Common response builder
    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now());
        response.put("status", status.value());
        response.put("message", message);

        return new ResponseEntity<>(response, status);
    }
}