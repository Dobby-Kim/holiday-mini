package com.holidaymini.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class HolidayMiniCustomException extends RuntimeException {

    private final HttpStatus httpStatus;

    public HolidayMiniCustomException(HttpStatus status, String message) {
        super(message);
        this.httpStatus = status;
    }
}
