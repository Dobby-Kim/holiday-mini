package com.holidaymini.external;

import static org.assertj.core.api.Assertions.assertThat;

import com.holidaymini.external.dto.CountryResponse;
import com.holidaymini.external.dto.PublicHolidayResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("NagerDateClient 통합 테스트")
class NagerDateClientIntegrationTest {

    @Autowired
    private NagerDateClient nagerDateClient;

    @Test
    @DisplayName("실제 API 호출 - 국가 목록 조회")
    void getAvailableCountries_RealAPI() {
        // when
        List<CountryResponse> countries = nagerDateClient.getAvailableCountries();

        // then
        assertThat(countries).isNotEmpty();
        assertThat(countries.get(0).countryCode()).isNotBlank();
        assertThat(countries.get(0).name()).isNotBlank();

        System.out.println("[DEBUG_LOG] 조회된 국가 수: " + countries.size());
        System.out.println("[DEBUG_LOG] 첫 번째 국가: " + countries.getFirst());
    }

    @Test
    @DisplayName("실제 API 호출 - 한국 2024년 공휴일 조회")
    void getPublicHolidays_RealAPI_Korea2024() {
        // when
        List<PublicHolidayResponse> holidays = nagerDateClient.getPublicHolidays(2024, "KR");

        // then
        assertThat(holidays).isNotEmpty();
        assertThat(holidays.get(0).countryCode()).isEqualTo("KR");
        assertThat(holidays.get(0).date()).isNotNull();
        assertThat(holidays.get(0).name()).isNotBlank();

        System.out.println("[DEBUG_LOG] 조회된 공휴일 수: " + holidays.size());
        System.out.println("[DEBUG_LOG] 첫 번째 공휴일: " + holidays.getFirst());
    }

    @Test
    @DisplayName("실제 API 호출 - 잘못된 국가 코드로 조회 시 빈 리스트 반환")
    void getPublicHolidays_RealAPI_InvalidCountryCode() {
        // when
        List<PublicHolidayResponse> holidays = nagerDateClient.getPublicHolidays(2024, "INVALID");

        // then
        assertThat(holidays).isEmpty();

        System.out.println("[DEBUG_LOG] 잘못된 국가 코드 조회 결과: " + holidays.size());
    }
}
