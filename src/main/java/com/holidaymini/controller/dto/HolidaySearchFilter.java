package com.holidaymini.controller.dto;

import com.holidaymini.domain.HolidayType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record HolidaySearchFilter(
        @NotNull
        String countryCode,

        @NotNull
        Integer year,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,

        HolidayType type
) {

}
