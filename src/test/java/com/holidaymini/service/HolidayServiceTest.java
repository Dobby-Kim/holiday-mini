package com.holidaymini.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.holidaymini.controller.dto.HolidaySearchFilter;
import com.holidaymini.domain.Country;
import com.holidaymini.domain.Holiday;
import com.holidaymini.domain.HolidayDetail;
import com.holidaymini.domain.HolidayType;
import com.holidaymini.exception.BadRequestException;
import com.holidaymini.exception.NotFoundException;
import com.holidaymini.repository.CountryRepository;
import com.holidaymini.repository.HolidayRepository;
import java.time.LocalDate;
import java.util.EnumSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class HolidayServiceTest {

    @Autowired
    private HolidayService holidayService;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private HolidayRepository holidayRepository;

    @BeforeEach
    void setUp() {
        // 초기 데이터 삭제
        holidayRepository.deleteAll();
        countryRepository.deleteAll();

        // 국가 저장
        Country kr = countryRepository.save(new Country("KR", "Korea"));
        Country us = countryRepository.save(new Country("US", "United States"));

        HolidayDetail detail1 = new HolidayDetail(true, false, "설날", EnumSet.of(HolidayType.PUBLIC));
        HolidayDetail detail2 = new HolidayDetail(false, true, "추석", EnumSet.of(HolidayType.PUBLIC));
        HolidayDetail detail3 = new HolidayDetail(
                true, true, "도엽 탄신일",
                EnumSet.of(HolidayType.PUBLIC, HolidayType.AUTHORITIES, HolidayType.BANK)
        );
        holidayRepository.save(new Holiday(kr, LocalDate.of(2025, 2, 12), "Lunar New Year", detail1));
        holidayRepository.save(new Holiday(kr, LocalDate.of(2025, 9, 21), "Chuseok", detail2));

        holidayRepository.save(new Holiday(kr, LocalDate.of(2020, 2, 14), "Dobby Birthday", detail3));
        holidayRepository.save(new Holiday(kr, LocalDate.of(2021, 2, 14), "Dobby Birthday", detail3));
        holidayRepository.save(new Holiday(kr, LocalDate.of(2022, 2, 14), "Dobby Birthday", detail3));
        holidayRepository.save(new Holiday(kr, LocalDate.of(2023, 2, 14), "Dobby Birthday", detail3));
        holidayRepository.save(new Holiday(kr, LocalDate.of(2024, 2, 14), "Dobby Birthday", detail3));
        holidayRepository.save(new Holiday(kr, LocalDate.of(2025, 2, 14), "Dobby Birthday", detail3));

        // US 2025 공휴일 1건
        HolidayDetail detail4 = new HolidayDetail(false, false, "Independence Day", EnumSet.of(HolidayType.PUBLIC));
        holidayRepository.save(new Holiday(us, LocalDate.of(2025, 7, 4), "Fourth of July", detail4));

        // KR 2022 공휴일 1건
        HolidayDetail detail5 = new HolidayDetail(true, false, "설날(22)", EnumSet.of(HolidayType.PUBLIC));
        holidayRepository.save(new Holiday(kr, LocalDate.of(2022, 2, 1), "Lunar New Year 2022", detail5));
    }

    @Test
    @DisplayName("KR 2025년 데이터 조회 시 3건이 반환된다")
    void testSearchByCountryYear_KR_2025() {
        // given
        HolidaySearchFilter filter = new HolidaySearchFilter("KR", 2025, null, null, null);
        PageRequest pr = PageRequest.of(0, 10, Sort.by("date").ascending());

        // when
        Page<Holiday> result = holidayService.searchHolidays(filter, pr);

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getContent()).extracting(Holiday::getName)
                .containsExactlyInAnyOrder("Lunar New Year", "Chuseok", "Dobby Birthday");
    }

    @Test
    @DisplayName("존재하지 않는 국가 코드로 조회 시 BadRequestException이 발생한다")
    void testSearch_InvalidCountryCode() {
        // given
        HolidaySearchFilter filter = new HolidaySearchFilter("ZZ", 2025, null, null, null);
        PageRequest pr = PageRequest.of(0, 10, Sort.by("date"));

        // then
        assertThatThrownBy(() -> holidayService.searchHolidays(filter, pr))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("등록되지 않은 국가 코드입니다");
    }

    @Test
    @DisplayName("범위 외 연도로 조회 시 NotFoundException이 발생한다")
    void testSearch_YearOutOfRange() {
        // given
        HolidaySearchFilter tooEarly = new HolidaySearchFilter("KR", 2019, null, null, null);
        HolidaySearchFilter tooLate = new HolidaySearchFilter("KR", 2026, null, null, null);
        PageRequest pr = PageRequest.of(0, 5, Sort.by("date"));

        // then
        assertThatThrownBy(() -> holidayService.searchHolidays(tooEarly, pr))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("2020 - 2025년 범위 외의 연도입니다.");

        assertThatThrownBy(() -> holidayService.searchHolidays(tooLate, pr))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("2020 - 2025년 범위 외의 연도입니다.");
    }

    @Test
    @DisplayName("날짜 범위 필터(startDate, endDate)로 조회 가능하다")
    void testSearch_ByDateRange() {
        // given
        LocalDate target = LocalDate.of(2025, 2, 14);
        HolidaySearchFilter filter = new HolidaySearchFilter("KR", 2025, target, target, null);
        PageRequest pr = PageRequest.of(0, 10, Sort.by("date"));

        // when
        Page<Holiday> result = holidayService.searchHolidays(filter, pr);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Dobby Birthday");
    }

    @Test
    @DisplayName("타입 필터(type)로 조회 가능하다")
    void testSearch_ByType() {
        // given
        HolidaySearchFilter filter = new HolidaySearchFilter("KR", 2025, null, null, HolidayType.AUTHORITIES);
        PageRequest pr = PageRequest.of(0, 10, Sort.by("date"));

        // when
        Page<Holiday> result = holidayService.searchHolidays(filter, pr);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).extracting(Holiday::getName)
                .containsExactly("Dobby Birthday");
    }
}
