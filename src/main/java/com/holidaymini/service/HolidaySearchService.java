package com.holidaymini.service;

import com.holidaymini.controller.dto.HolidaySearchFilter;
import com.holidaymini.domain.Country;
import com.holidaymini.domain.Holiday;
import com.holidaymini.exception.BadRequestException;
import com.holidaymini.exception.NotFoundException;
import com.holidaymini.repository.CountryRepository;
import com.holidaymini.repository.HolidayRepository;
import com.holidaymini.repository.dto.HolidaySearchCondition;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HolidaySearchService {

    private static final int START_YEAR = 2020;
    private static final int END_YEAR = 2025;

    private final NagerDataLoadService nagerDataLoadService;
    private final HolidayRepository holidayRepository;
    private final CountryRepository countryRepository;

    @Transactional(readOnly = true)
    public Page<Holiday> searchHolidays(HolidaySearchFilter request, Pageable pageable) {
        validateYear(request.year());

        Country country = countryRepository.findById(request.countryCode())
                .orElseThrow(() -> new BadRequestException("등록되지 않은 국가 코드입니다"));

        HolidaySearchCondition condition = convertToCondition(country, request);
        return holidayRepository.searchByConditions(condition, pageable);
    }

    private void validateYear(int year) {
        if (year < START_YEAR || END_YEAR < year) {
            throw new NotFoundException("2020 - 2025년 범위 외의 연도입니다.");
        }
    }

    private HolidaySearchCondition convertToCondition(Country country, HolidaySearchFilter request) {
        return HolidaySearchCondition.builder()
                .country(country)
                .year(request.year())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .type(request.type())
                .build();
    }

    public void upsertByCountryCodeAndYear(String countryCode, int year) {
        Country targetCountry = countryRepository.findById(countryCode)
                .orElseThrow(() -> new BadRequestException("존재하지 않는 국가 코드입니다"));

        List<Holiday> existHolidays = holidayRepository.findByCountryAndYear(targetCountry, year);

        nagerDataLoadService.loadHolidaysByCountryAndYear(targetCountry, year);

        holidayRepository.deleteAll(existHolidays);
    }

    public void deleteByCountryCodeAndYear(String countryCode, Integer year) {
        Country targetCountry = countryRepository.findById(countryCode)
                .orElseThrow(() -> new BadRequestException("존재하지 않는 국가 코드입니다"));

        List<Holiday> existHolidays = holidayRepository.findByCountryAndYear(targetCountry, year);
        holidayRepository.deleteAll(existHolidays);
    }
}
