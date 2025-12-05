package com.nutrigo.nutrigo_backend.global.error;

import com.nutrigo.nutrigo_backend.global.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<String>> handleAppException(AppException e) {
        return ResponseEntity.status(e.getStatus())
                .body(ApiResponse.fail(e.getErrorCode(), e.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<String>> handleBadCredentials(BadCredentialsException e) {
        AppException invalidCredentials = new AppExceptions.Auth.InvalidCredentialsException();
        return ResponseEntity.status(invalidCredentials.getStatus())
                .body(ApiResponse.fail(invalidCredentials.getErrorCode(), invalidCredentials.getMessage()));
    }

    private static final String VALIDATION_ERROR_CODE = "COMMON_001";
    private static final String CONSTRAINT_VIOLATION_CODE = "COMMON_002";
    private static final String MALFORMED_REQUEST_CODE = "COMMON_003";
    private static final String BAD_REQUEST_CODE = "COMMON_004";
    private static final String METHOD_NOT_ALLOWED_CODE = "COMMON_005";
    private static final String CONFLICT_ERROR_CODE = "COMMON_006";
    private static final String DATA_INTEGRITY_CODE = "COMMON_007";
    private static final String INTERNAL_SERVER_ERROR_CODE = "COMMON_999";
    private static final Map<String, String> VALIDATION_ERROR_CODES = Map.ofEntries(
            Map.entry("이메일을 입력해주세요", "VALIDATION_001"),
            Map.entry("이메일 형식이 올바르지 않습니다", "VALIDATION_002"),
            Map.entry("비밀번호를 입력해주세요", "VALIDATION_003"),
            Map.entry("닉네임을 입력해주세요", "VALIDATION_004"),
            Map.entry("이름을 입력해주세요", "VALIDATION_005"),
            Map.entry("생년월일은 과거 또는 오늘 날짜여야 합니다", "VALIDATION_006"),
            Map.entry("소셜 로그인 제공자를 선택해주세요", "VALIDATION_007"),
            Map.entry("리프레시 토큰을 입력해주세요", "VALIDATION_008"),
            Map.entry("챌린지 제목을 입력해주세요", "VALIDATION_009"),
            Map.entry("챌린지 카테고리를 선택해주세요", "VALIDATION_010"),
            Map.entry("챌린지 유형을 선택해주세요", "VALIDATION_011"),
            Map.entry("챌린지 기간을 입력해주세요", "VALIDATION_012"),
            Map.entry("기간은 1일 이상이어야 합니다", "VALIDATION_013"),
            Map.entry("목표 횟수는 1 이상이어야 합니다", "VALIDATION_014"),
            Map.entry("최대 칼로리는 1 이상이어야 합니다", "VALIDATION_015"),
            Map.entry("최대 나트륨은 1 이상이어야 합니다", "VALIDATION_016"),
            Map.entry("닉네임은 1자 이상 50자 이하여야 합니다", "VALIDATION_017"),
            Map.entry("이름은 1자 이상 50자 이하여야 합니다", "VALIDATION_018"),
            Map.entry("주소는 255자 이하여야 합니다", "VALIDATION_019")
    );

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(BAD_REQUEST_CODE, e.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResponse<String>> handleValidation(Exception e) {
        String message = "Invalid request";
        String errorCode = VALIDATION_ERROR_CODE;
        if (e instanceof MethodArgumentNotValidException exception) {
            FieldError fieldError = extractFirstFieldError(exception.getBindingResult());
            if (fieldError != null) {
                message = formatFieldError(fieldError);
                errorCode = resolveValidationCode(fieldError.getDefaultMessage());
            }
        } else if (e instanceof BindException exception) {
            FieldError fieldError = extractFirstFieldError(exception.getBindingResult());
            if (fieldError != null) {
                message = formatFieldError(fieldError);
                errorCode = resolveValidationCode(fieldError.getDefaultMessage());
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(errorCode, message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .findFirst()
                .orElse("Constraint violation");
        String errorCode = e.getConstraintViolations().stream()
                .map(violation -> resolveValidationCode(violation.getMessage()))
                .findFirst()
                .orElse(CONSTRAINT_VIOLATION_CODE);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(errorCode, message));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiResponse<String>> handleMalformedRequest(Exception e) {
        String message = e instanceof MissingServletRequestParameterException missing
                ? "Missing request parameter: " + missing.getParameterName()
                : "Malformed request payload";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(MALFORMED_REQUEST_CODE, message));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<String>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.fail(METHOD_NOT_ALLOWED_CODE, "Request method not supported"));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<String>> handleConflict(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail(CONFLICT_ERROR_CODE, e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("Data integrity violation", e);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail(DATA_INTEGRITY_CODE, "Request conflicts with current data state"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleException(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(INTERNAL_SERVER_ERROR_CODE, "Internal server error"));
    }

    private FieldError extractFirstFieldError(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .findFirst()
                .orElse(null);
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    private String resolveValidationCode(String message) {
        return VALIDATION_ERROR_CODES.getOrDefault(message, VALIDATION_ERROR_CODE);
    }
}