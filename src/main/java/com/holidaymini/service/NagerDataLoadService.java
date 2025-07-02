package com.holidaymini.service;

import com.holidaymini.domain.Country;
import com.holidaymini.domain.Holiday;
import com.holidaymini.domain.HolidayDetail;
import com.holidaymini.domain.HolidayType;
import com.holidaymini.external.nager.NagerDateClient;
import com.holidaymini.external.nager.dto.CountryResponse;
import com.holidaymini.external.nager.dto.PublicHolidayResponse;
import com.holidaymini.repository.CountryRepository;
import com.holidaymini.repository.HolidayRepository;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NagerDataLoadService {

    private final NagerDateClient nagerDateClient;
    private final CountryRepository countryRepository;
    private final HolidayRepository holidayRepository;

    private final AtomicInteger successfulCountries = new AtomicInteger(0);
    private final AtomicInteger failedCountries = new AtomicInteger(0);

    @Transactional
    public Set<Country> loadCountries() {
        List<CountryResponse> countryResponses = nagerDateClient.getAvailableCountries();
        Set<Country> countries = convertToCountries(countryResponses);
        countryRepository.saveAll(countries);
        log.info("{}개 국가 정보를 저장했습니다.", countries.size());
        return countries;
    }

    @Transactional
    public void loadHolidaysByCountryAndYear(Country country, int year) {
        try {
            List<PublicHolidayResponse> holidayResponses = nagerDateClient.getPublicHolidays(
                    year,
                    country.getCountryCode()
            );
            List<Holiday> holidays = convertToHolidays(holidayResponses, country);
            holidayRepository.saveAll(holidays);
            log.debug("{}년 {} 공휴일 {}개 저장", year, country.getCountryCode(), holidays.size());

            successfulCountries.incrementAndGet();
        } catch (Exception e) {
            log.warn("국가 {}의 공휴일 데이터 로드 실패: {}", country.getCountryCode(), e.getMessage());
            failedCountries.incrementAndGet();
        }
    }

    private Set<Country> convertToCountries(List<CountryResponse> countryResponses) {
        return countryResponses.stream()
                .map(response -> new Country(response.countryCode(), response.name()))
                .collect(Collectors.toSet());
    }

    private List<Holiday> convertToHolidays(List<PublicHolidayResponse> holidayResponses, Country country) {
        return holidayResponses.stream()
                .map(response -> convertToHoliday(response, country))
                .toList();
    }

    private Holiday convertToHoliday(PublicHolidayResponse response, Country country) {
        Set<HolidayType> types = response.types()
                .stream()
                .map(HolidayType::convert)
                .collect(Collectors.toSet());

        HolidayDetail detail = new HolidayDetail(
                response.isFixed(),
                response.isGlobal(),
                response.localName(),
                types
        );

        return new Holiday(country, response.date(), response.name(), detail);
    }

}
