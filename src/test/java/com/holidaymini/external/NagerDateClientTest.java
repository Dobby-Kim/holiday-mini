package com.holidaymini.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.holidaymini.exception.InternalServerException;
import com.holidaymini.external.dto.CountryResponse;
import com.holidaymini.external.dto.PublicHolidayResponse;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersUriSpec;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
@DisplayName("NagerDateClient 테스트")
class NagerDateClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private NagerDateClient nagerDateClient;

    @BeforeEach
    void setUp() {
        nagerDateClient = new NagerDateClient(restClient);
    }

    @Test
    @DisplayName("국가 목록 조회 성공")
    void getAvailableCountries_Success() {
        // given
        List<CountryResponse> expectedCountries = List.of(
                new CountryResponse("KR", "South Korea"),
                new CountryResponse("US", "United States")
        );

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/AvailableCountries")).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(expectedCountries);

        // when
        List<CountryResponse> result = nagerDateClient.getAvailableCountries();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().countryCode()).isEqualTo("KR");
        assertThat(result.getFirst().name()).isEqualTo("South Korea");
    }

    @Test
    @DisplayName("국가 목록 조회 실패 시 빈 리스트 반환")
    void getAvailableCountries_ClientError_ReturnsEmptyList() {
        // given
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/AvailableCountries")).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("클라이언트 오류"));

        // when
        List<CountryResponse> result = nagerDateClient.getAvailableCountries();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("공휴일 조회 성공")
    void getPublicHolidays_Success() {
        // given
        int year = 2024;
        String countryCode = "KR";
        List<PublicHolidayResponse> expectedHolidays = List.of(
                new PublicHolidayResponse(
                        LocalDate.of(2024, 1, 1),
                        "신정",
                        "New Year's Day",
                        "KR",
                        List.of("Public")
                )
        );

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/PublicHolidays/{year}/{countryCode}", year, countryCode))
                .thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(expectedHolidays);

        // when
        List<PublicHolidayResponse> result = nagerDateClient.getPublicHolidays(year, countryCode);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).date()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(result.get(0).localName()).isEqualTo("신정");
        assertThat(result.get(0).countryCode()).isEqualTo("KR");
    }

    @Test
    @DisplayName("공휴일 조회 실패 시 빈 리스트 반환")
    void getPublicHolidays_ClientError_ReturnsEmptyList() {
        // given
        int year = 2024;
        String countryCode = "INVALID";

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/PublicHolidays/{year}/{countryCode}", year, countryCode))
                .thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("클라이언트 오류"));

        // when
        List<PublicHolidayResponse> result = nagerDateClient.getPublicHolidays(year, countryCode);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("서버 오류 시 InternalServerException 발생")
    void getAvailableCountries_ServerError_ThrowsInternalServerException() {
        // given
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/AvailableCountries")).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenThrow(new InternalServerException("외부 API 서버 오류로 인해 국가 목록을 조회할 수 없습니다."));

        // when & then
        assertThatThrownBy(() -> nagerDateClient.getAvailableCountries())
                .isInstanceOf(InternalServerException.class)
                .hasMessage("외부 API 서버 오류로 인해 국가 목록을 조회할 수 없습니다.");
    }
}
