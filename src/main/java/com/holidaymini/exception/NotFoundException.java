package com.holidaymini.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends HolidayMiniCustomException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;

    public NotFoundException(String message) {
        super(STATUS, message);
    }
}
