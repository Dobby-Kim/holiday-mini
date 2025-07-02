package com.holidaymini.external.nager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;

public record PublicHolidayResponse(
        @JsonProperty("date")
        LocalDate date,

        @JsonProperty("localName")
        String localName,

        @JsonProperty("name")
        String name,

        @JsonProperty("countryCode")
        String countryCode,

        @JsonProperty("fixed")
        Boolean isFixed,

        @JsonProperty("global")
        Boolean isGlobal,

        @JsonProperty("types")
        List<String> types
) {

}
