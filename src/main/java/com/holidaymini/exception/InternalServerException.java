package com.holidaymini.exception;

import org.springframework.http.HttpStatus;

public class InternalServerException extends HolidayMiniCustomException {

    private static final HttpStatus STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

    public InternalServerException(String message) {
        super(STATUS, message);
    }
}
