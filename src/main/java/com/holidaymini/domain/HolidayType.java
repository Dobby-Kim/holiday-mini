package com.holidaymini.domain;

import lombok.Getter;

@Getter
public enum HolidayType {

    PUBLIC("Public"),
    BANK("Bank"),
    SCHOOL("School"),
    AUTHORITIES("Authorities"),
    OPTIONAL("Optional"),
    OBSERVANCE("Observance")
    ;

    private final String typeName;

    HolidayType(String typeName) {
        this.typeName = typeName;
    }
}
