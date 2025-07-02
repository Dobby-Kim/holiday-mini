package com.holidaymini.controller.dto;

import com.holidaymini.domain.HolidayType;
import java.time.LocalDate;
import java.util.Set;

public record HolidayResponse(
        Long id,
        String name,
        String localName,
        LocalDate date,
        Integer year,
        String countryCode,
        String countryName,
        Boolean isFixed,
        Boolean isGlobal,
        Set<HolidayType> types
) {

}
