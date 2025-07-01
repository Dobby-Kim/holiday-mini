package com.holidaymini.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InternalServerException extends HolidayMiniCustomException {

    private static final HttpStatus STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

    public InternalServerException(String message) {
        super(STATUS, message);
    }
}
