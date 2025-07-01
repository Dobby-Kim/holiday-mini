package com.holidaymini.repository.dto;

import com.holidaymini.domain.Country;
import com.holidaymini.domain.HolidayType;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HolidaySearchCondition {
    private Country country;
    private Integer year;
    private LocalDate startDate;
    private LocalDate endDate;
    private HolidayType type;
}
