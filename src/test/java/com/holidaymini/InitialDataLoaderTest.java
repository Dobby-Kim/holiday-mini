package com.holidaymini;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.holidaymini.domain.Country;
import com.holidaymini.external.nager.NagerDateClient;
import com.holidaymini.external.nager.dto.CountryResponse;
import com.holidaymini.external.nager.dto.PublicHolidayResponse;
import com.holidaymini.repository.CountryRepository;
import com.holidaymini.repository.HolidayRepository;
import com.holidaymini.service.NagerDataLoadService;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InitialDataLoaderTest {

    @Autowired
    private NagerDataLoadService dataLoadService;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private HolidayRepository holidayRepository;

    @Mock
    private NagerDateClient nagerDateClient;

    private InitialDataLoader initialDataLoader;

    @BeforeEach
    void setUp() {
        // 테스트용 동기 실행 Executor 사용 (예측 가능한 테스트를 위해)
        Executor testExecutor = Runnable::run;
        initialDataLoader = new InitialDataLoader(dataLoadService, testExecutor);

        // 기존 데이터 정리
        holidayRepository.deleteAll();
        countryRepository.deleteAll();
    }

    @Test
    @DisplayName("정상적인 초기 데이터 로드 테스트 - 모든 국가와 휴일 데이터가 성공적으로 저장되어야 한다")
    void run_normalDataLoadingSuccess() {
        // Given
        List<CountryResponse> mockCountryResponses = List.of(
                new CountryResponse("KR", "South Korea"),
                new CountryResponse("US", "United States"),
                new CountryResponse("JP", "Japan")
        );

        List<PublicHolidayResponse> mockHolidayResponses = List.of(
                createMockHolidayResponse("New Year's Day", LocalDate.of(2024, 1, 1)),
                createMockHolidayResponse("Independence Day", LocalDate.of(2024, 7, 4))
        );

        when(nagerDateClient.getAvailableCountries()).thenReturn(mockCountryResponses);
        when(nagerDateClient.getPublicHolidays(anyInt(), anyString())).thenReturn(mockHolidayResponses);

        // When
        assertDoesNotThrow(() -> initialDataLoader.run(null));

        // Then
        // 국가 데이터 검증
        List<Country> savedCountries = countryRepository.findAll();
        assertEquals(3, savedCountries.size());

        Set<String> countryCodes = savedCountries.stream()
                .map(Country::getCountryCode)
                .collect(java.util.stream.Collectors.toSet());
        assertTrue(countryCodes.containsAll(Set.of("KR", "US", "JP")));

        // 휴일 데이터 검증 (2020-2025년, 3개국, 각각 2개 휴일)
        long expectedHolidayCount = 6 * 3 * 2; // 6년 * 3개국 * 2개 휴일
        assertEquals(expectedHolidayCount, holidayRepository.count());

        // API 호출 검증
        verify(nagerDateClient, times(1)).getAvailableCountries();
        verify(nagerDateClient, times(18)).getPublicHolidays(anyInt(), anyString());
    }

    @Test
    @DisplayName("국가 데이터 로드 실패 시 예외 처리 테스트 - API 호출 실패 시 안전하게 종료되어야 한다")
    void run_countryLoadingFailure() {
        // Given
        when(nagerDateClient.getAvailableCountries())
                .thenThrow(new RuntimeException("API 호출 실패"));

        // When & Then
        assertDoesNotThrow(() -> initialDataLoader.run(null));

        // 데이터베이스에 저장된 데이터가 없어야 함
        assertEquals(0, countryRepository.count());
        assertEquals(0, holidayRepository.count());

        verify(nagerDateClient, times(1)).getAvailableCountries();
        verify(nagerDateClient, never()).getPublicHolidays(anyInt(), anyString());
    }

    @Test
    @DisplayName("특정 국가 휴일 데이터 로드 실패 시 다른 국가는 정상 처리 테스트 - 일부 실패가 전체에 영향을 주지 않아야 한다")
    void run_partialHolidayLoadingFailure() {
        // Given
        List<CountryResponse> mockCountryResponses = List.of(
                new CountryResponse("KR", "South Korea"),
                new CountryResponse("INVALID", "Invalid Country")
        );

        List<PublicHolidayResponse> validHolidayResponses = List.of(
                createMockHolidayResponse("Korean Holiday", LocalDate.of(2024, 1, 1))
        );

        when(nagerDateClient.getAvailableCountries()).thenReturn(mockCountryResponses);
        when(nagerDateClient.getPublicHolidays(anyInt(), eq("KR"))).thenReturn(validHolidayResponses);
        when(nagerDateClient.getPublicHolidays(anyInt(), eq("INVALID")))
                .thenThrow(new RuntimeException("Invalid country"));

        // When
        assertDoesNotThrow(() -> initialDataLoader.run(null));

        // Then
        // 국가 데이터는 모두 저장되어야 함
        assertEquals(2, countryRepository.count());

        // 성공한 국가의 휴일만 저장되어야 함 (KR만 6년 * 1개 휴일)
        assertEquals(6, holidayRepository.count());

        verify(nagerDateClient, times(1)).getAvailableCountries();
        verify(nagerDateClient, times(12)).getPublicHolidays(anyInt(), anyString());
    }

    @Test
    @DisplayName("빈 국가 목록 처리 테스트 - 국가 데이터가 없을 때 안전하게 처리되어야 한다")
    void run_emptyCountryList() {
        // Given
        when(nagerDateClient.getAvailableCountries()).thenReturn(List.of());

        // When
        assertDoesNotThrow(() -> initialDataLoader.run(null));

        // Then
        assertEquals(0, countryRepository.count());
        assertEquals(0, holidayRepository.count());

        verify(nagerDateClient, times(1)).getAvailableCountries();
        verify(nagerDateClient, never()).getPublicHolidays(anyInt(), anyString());
    }

    @Test
    @DisplayName("모든 국가 휴일 로드 실패 테스트 - 휴일 API가 전체적으로 실패해도 국가 데이터는 저장되어야 한다")
    void run_allHolidayLoadingFailure() {
        // Given
        List<CountryResponse> mockCountryResponses = List.of(
                new CountryResponse("KR", "South Korea")
        );

        when(nagerDateClient.getAvailableCountries()).thenReturn(mockCountryResponses);
        when(nagerDateClient.getPublicHolidays(anyInt(), anyString()))
                .thenThrow(new RuntimeException("서버 오류"));

        // When
        assertDoesNotThrow(() -> initialDataLoader.run(null));

        // Then
        // 국가 데이터는 저장되어야 함
        assertEquals(1, countryRepository.count());
        // 휴일 데이터는 저장되지 않아야 함
        assertEquals(0, holidayRepository.count());

        verify(nagerDateClient, times(1)).getAvailableCountries();
        verify(nagerDateClient, times(6)).getPublicHolidays(anyInt(), anyString());
    }

    private PublicHolidayResponse createMockHolidayResponse(String name, LocalDate date) {
        return new PublicHolidayResponse(
                date,
                name,
                name,
                "KR",
                true,
                true,
                List.of("Public")
        );
    }
}

