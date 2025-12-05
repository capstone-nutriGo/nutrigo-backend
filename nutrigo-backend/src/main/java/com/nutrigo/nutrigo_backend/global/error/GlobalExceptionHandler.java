package com.nutrigo.nutrigo_backend.global.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException e) {
        // Authorization 관련 에러는 401 Unauthorized로 반환
        if (e.getMessage() != null &&
                (e.getMessage().contains("Authorization") ||
                        e.getMessage().contains("missing") ||
                        e.getMessage().contains("invalid"))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: " + e.getMessage());
        }
        // 기타 IllegalStateException은 400 Bad Request로 반환
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Bad request: " + e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Bad request: " + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        // TODO: 나중에 에러 응답 포맷 정의해서 교체
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error: " + e.getMessage());
    }
}
