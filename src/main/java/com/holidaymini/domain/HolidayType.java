package com.holidaymini.domain;

import java.util.Arrays;
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

    public static HolidayType convert(String typeString) {
        return Arrays.stream(HolidayType.values())
                .filter(type -> type.typeName.equals(typeString))
                .findFirst()
                .orElse(PUBLIC);
    }
}
