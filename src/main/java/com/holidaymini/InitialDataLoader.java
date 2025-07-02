package com.holidaymini;

import com.holidaymini.domain.Country;
import com.holidaymini.service.NagerDataLoadService;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class InitialDataLoader implements ApplicationRunner {

    private static final int START_YEAR = 2020;
    private static final int END_YEAR = 2025;

    private final NagerDataLoadService dataLoadService;
    private final Executor loaderExecutor;

    @Value("${data-loader.active}")
    private boolean isActive;

    @Override
    public void run(ApplicationArguments args) {
        if(!isActive) {
            return;
        }

        log.info("애플리케이션 시작 시 초기 데이터 로드를 시작합니다.");
        try {
            Set<Country> countries = dataLoadService.loadCountries();
            for (int year = START_YEAR; year <= END_YEAR; year++) {
                loadAllByYearAsync(countries, year);
            }
        } catch (Exception e) {
            log.error("초기 데이터 로드 중 오류가 발생했습니다.", e);
        }
    }

    private void loadAllByYearAsync(Set<Country> countries, int year) {
        var futures = countries.stream()
                .map(country -> loadCountryDataAsync(country, year))
                .toList();

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .join();

        log.info("{}년도 초기 데이터 로드가 성공적으로 완료되었습니다. ({}개 국가)", year, countries.size());
    }

    private CompletableFuture<Void> loadCountryDataAsync(Country country, int year) {
        return CompletableFuture
                .runAsync(() -> dataLoadService.loadHolidaysByCountryAndYear(country, year), loaderExecutor)
                .exceptionally(ex -> {
                    log.warn("{}년도 {} 데이터 로드 실패: {}", year, country.getCountryCode(), ex.getMessage());
                    return null;
                });
    }
}
