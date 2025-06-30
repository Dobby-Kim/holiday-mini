package com.holidaymini.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handle(final HttpServletRequest request) {
        final HttpStatus httpStatus = HttpStatus.NOT_IMPLEMENTED;
        final Map<String, Object> body = Map.of(
                "timestamp", Instant.now(),
                "status", httpStatus.value(),
                "error", httpStatus.getReasonPhrase(),
                "path", request.getRequestURI()
        );
        return ResponseEntity.status(httpStatus).body(body);
    }

    @ExceptionHandler(HolidayMiniCustomException.class)
    public ResponseEntity<Map<String, Object>> handleCustomException(final HolidayMiniCustomException exception, final HttpServletRequest request) {
        final HttpStatus httpStatus = exception.getHttpStatus();
        final Map<String, Object> body = Map.of(
                "timestamp", Instant.now(),
                "status", httpStatus.value(),
                "error", httpStatus.getReasonPhrase(),
                "path", request.getRequestURI()
        );
        return ResponseEntity.status(httpStatus).body(body);
    }


}
