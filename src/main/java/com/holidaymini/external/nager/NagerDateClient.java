package com.holidaymini.external.nager;

import com.holidaymini.exception.InternalServerException;
import com.holidaymini.external.HolidayApiClient;
import com.holidaymini.external.nager.dto.CountryResponse;
import com.holidaymini.external.nager.dto.PublicHolidayResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NagerDateClient implements HolidayApiClient {

    private final RestClient restClient;

    public List<CountryResponse> getAvailableCountries() {
        return fetchList(
                NagerApiEndpoint.GET_AVAILABLE_COUNTRIES.getUrl(),
                new ParameterizedTypeReference<List<CountryResponse>>() {},
                "국가 목록 조회 실패"
        );
    }

    public List<PublicHolidayResponse> getPublicHolidays(int year, String countryCode) {
        return fetchList(
                NagerApiEndpoint.GET_PUBLIC_HOLIDAYS.getUrl(),
                new ParameterizedTypeReference<List<PublicHolidayResponse>>() {},
                "공휴일 조회 실패",
                year, countryCode
        );
    }

    private <T> List<T> fetchList(
            String urlTemplate,
            ParameterizedTypeReference<List<T>> typeRef,
            String contextMsg,
            Object... uriVars
    ) {
        try {
            return restClient.get()
                    .uri(urlTemplate, uriVars)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        log.warn("{} - 클라이언트 오류: {}", contextMsg, res.getStatusCode());
                        throw new RestClientException("클라이언트 오류");
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        log.error("{} - 서버 오류: {}", contextMsg, res.getStatusCode());
                        throw new InternalServerException(
                                "외부 API 서버 오류로 인해 " +
                                        contextMsg.replace(" 조회 실패", "") +
                                        "을(를) 조회할 수 없습니다."
                        );
                    })
                    .body(typeRef);
        } catch (RestClientException e) {
            log.warn("{} - 빈 리스트 반환", contextMsg);
            return Collections.emptyList();
        } catch (InternalServerException e) {
            throw e;
        } catch (Exception e) {
            log.error("{} 중 예기치 못한 오류 발생", contextMsg, e);
            throw new InternalServerException(contextMsg + " 중 오류가 발생했습니다.");
        }
    }
}
