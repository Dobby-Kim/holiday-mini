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

    /**
     * Constructs a HolidayType enum constant with the specified descriptive name.
     *
     * @param typeName the human-readable name associated with the holiday type
     */
    HolidayType(String typeName) {
        this.typeName = typeName;
    }
}
