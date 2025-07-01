package com.holidaymini.external.nager.dto;

import java.time.LocalDate;
import java.util.List;

public record PublicHolidayResponse(
        LocalDate date,
        String localName,
        String name,
        String countryCode,
        List<String> types
) {

}
