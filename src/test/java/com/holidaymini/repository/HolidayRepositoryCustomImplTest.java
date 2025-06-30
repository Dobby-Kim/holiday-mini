package com.holidaymini.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.holidaymini.domain.Country;
import com.holidaymini.domain.Holiday;
import com.holidaymini.domain.HolidayDetail;
import com.holidaymini.domain.HolidayType;
import com.holidaymini.repository.dto.HolidaySearchCondition;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@DisplayName("HolidayRepositoryCustomImpl 테스트")
class HolidayRepositoryCustomImplTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private HolidayRepositoryCustomImpl holidayRepositoryCustom;

    private Country korea;
    private Country usa;
    private Country japan;

    @BeforeEach
    void setUp() {
        // Given: 테스트 데이터 준비
        korea = new Country("KR", "대한민국");
        usa = new Country("US", "미국");
        japan = new Country("JP", "일본");

        entityManager.persist(korea);
        entityManager.persist(usa);
        entityManager.persist(japan);

        // 한국 휴일 데이터
        Holiday koreanNewYear = new Holiday(
                korea,
                LocalDate.of(2023, 1, 1),
                "신정",
                new HolidayDetail(true, true, null, "신정", Set.of(HolidayType.PUBLIC))
        );

        Holiday koreanChildrensDay = new Holiday(
                korea,
                LocalDate.of(2023, 5, 5),
                "어린이날",
                new HolidayDetail(true, false, 1975, "어린이날", Set.of(HolidayType.PUBLIC, HolidayType.SCHOOL))
        );

        Holiday koreanChusuk = new Holiday(
                korea,
                LocalDate.of(2023, 9, 29),
                "추석",
                new HolidayDetail(false, false, null, "추석", Set.of(HolidayType.PUBLIC))
        );

        // 미국 휴일 데이터
        Holiday usNewYear = new Holiday(
                usa,
                LocalDate.of(2023, 1, 1),
                "New Year's Day",
                new HolidayDetail(true, true, null, "New Year's Day", Set.of(HolidayType.PUBLIC, HolidayType.BANK))
        );

        Holiday usIndependenceDay = new Holiday(
                usa,
                LocalDate.of(2023, 7, 4),
                "Independence Day",
                new HolidayDetail(true, true, 1776, "Independence Day", Set.of(HolidayType.PUBLIC))
        );

        // 일본 휴일 데이터 (2024년)
        Holiday japanNewYear = new Holiday(
                japan,
                LocalDate.of(2024, 1, 1),
                "元日",
                new HolidayDetail(true, true, null, "元日", Set.of(HolidayType.PUBLIC))
        );

        Holiday japanGoldenWeek = new Holiday(
                japan,
                LocalDate.of(2024, 5, 3),
                "憲法記念日",
                new HolidayDetail(true, true, 1947, "憲法記念日", Set.of(HolidayType.PUBLIC, HolidayType.OBSERVANCE))
        );

        // 은행 휴일 데이터
        Holiday bankHoliday = new Holiday(
                usa,
                LocalDate.of(2023, 12, 25),
                "Christmas Day",
                new HolidayDetail(true, true, null, "Christmas Day", Set.of(HolidayType.BANK))
        );

        entityManager.persist(koreanNewYear);
        entityManager.persist(koreanChildrensDay);
        entityManager.persist(koreanChusuk);
        entityManager.persist(usNewYear);
        entityManager.persist(usIndependenceDay);
        entityManager.persist(japanNewYear);
        entityManager.persist(japanGoldenWeek);
        entityManager.persist(bankHoliday);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("국가별 휴일 검색이 정상적으로 동작한다")
    void searchByCountry() {
        // Given
        HolidaySearchCondition condition = HolidaySearchCondition.builder().country(korea).build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Holiday> result = holidayRepositoryCustom.searchByConditions(condition, pageable);

        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).extracting(Holiday::getCountry).containsOnly(korea);
        assertThat(result.getContent()).extracting(Holiday::getName).containsExactly("신정", "어린이날", "추석");
    }

    @Test
    @DisplayName("연도별 휴일 검색이 정상적으로 동작한다")
    void searchByYear() {
        // Given
        HolidaySearchCondition condition = HolidaySearchCondition.builder().year(2023).build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Holiday> result = holidayRepositoryCustom.searchByConditions(condition, pageable);

        // Then
        assertThat(result.getContent()).hasSize(6);
        assertThat(result.getContent()).extracting(Holiday::getYear).containsOnly(2023);
    }

    @Test
    @DisplayName("날짜 범위로 휴일 검색이 정상적으로 동작한다")
    void searchByDateRange() {
        // Given
        HolidaySearchCondition condition = HolidaySearchCondition.builder().startDate(LocalDate.of(2023, 5, 1)).endDate(
                LocalDate.of(2023, 9, 30)).build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Holiday> result = holidayRepositoryCustom.searchByConditions(condition, pageable);

        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).extracting(Holiday::getName).containsExactly("어린이날", "Independence Day", "추석");
    }

    @Test
    @DisplayName("시작 날짜만으로 휴일 검색이 정상적으로 동작한다")
    void searchByStartDateOnly() {
        // Given
        HolidaySearchCondition condition = HolidaySearchCondition.builder().startDate(LocalDate.of(2023, 7, 1)).build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Holiday> result = holidayRepositoryCustom.searchByConditions(condition, pageable);

        // Then
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getContent()).extracting(Holiday::getDate).allMatch(date -> !date.isBefore(LocalDate.of(
                2023,
                7,
                1
        )));
    }

    @Test
    @DisplayName("종료 날짜만으로 휴일 검색이 정상적으로 동작한다")
    void searchByEndDateOnly() {
        // Given
        HolidaySearchCondition condition = HolidaySearchCondition.builder().endDate(LocalDate.of(2023, 5, 31)).build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Holiday> result = holidayRepositoryCustom.searchByConditions(condition, pageable);

        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).extracting(Holiday::getDate).allMatch(date -> !date.isAfter(LocalDate.of(
                2023,
                5,
                31
        )));
    }

    @Test
    @DisplayName("휴일 타입별 검색이 정상적으로 동작한다")
    void searchByHolidayType() {
        // Given
        HolidaySearchCondition condition = HolidaySearchCondition.builder().type(HolidayType.BANK).build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Holiday> result = holidayRepositoryCustom.searchByConditions(condition, pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(Holiday::getName).containsExactlyInAnyOrder(
                "New Year's Day",
                "Christmas Day"
        );
    }

    @Test
    @DisplayName("SCHOOL 타입 휴일 검색이 정상적으로 동작한다")
    void searchBySchoolType() {
        // Given
        HolidaySearchCondition condition = HolidaySearchCondition.builder().type(HolidayType.SCHOOL).build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Holiday> result = holidayRepositoryCustom.searchByConditions(condition, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("어린이날");
    }

    @Test
    @DisplayName("복합 조건으로 휴일 검색이 정상적으로 동작한다")
    void searchByMultipleConditions() {
        // Given
        HolidaySearchCondition condition = HolidaySearchCondition.builder().country(usa).year(2023)
                .type(HolidayType.PUBLIC).build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Holiday> result = holidayRepositoryCustom.searchByConditions(condition, pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(Holiday::getName).containsExactlyInAnyOrder(
                "New Year's Day",
                "Independence Day"
        );
        assertThat(result.getContent()).extracting(Holiday::getCountry).containsOnly(usa);
    }

    @Test
    @DisplayName("조건이 없을 때 전체 휴일이 조회된다")
    void searchWithoutConditions() {
        // Given
        HolidaySearchCondition condition = HolidaySearchCondition.builder().build();
        Pageable pageable = PageRequest.of(0, 20);

        // When
        Page<Holiday> result = holidayRepositoryCustom.searchByConditions(condition, pageable);

        // Then
        assertThat(result.getContent()).hasSize(8);
        assertThat(result.getTotalElements()).isEqualTo(8);
    }

    @Test
    @DisplayName("페이징이 정상적으로 동작한다")
    void searchWithPaging() {
        // Given
        HolidaySearchCondition condition = HolidaySearchCondition.builder().build();
        Pageable pageable = PageRequest.of(0, 3);

        // When
        Page<Holiday> result = holidayRepositoryCustom.searchByConditions(condition, pageable);

        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(8);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("두 번째 페이지 조회가 정상적으로 동작한다")
    void searchSecondPage() {
        // Given
        HolidaySearchCondition condition = HolidaySearchCondition.builder().build();
        Pageable pageable = PageRequest.of(1, 3);

        // When
        Page<Holiday> result = holidayRepositoryCustom.searchByConditions(condition, pageable);

        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(8);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.hasPrevious()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 국가로 검색하면 빈 결과가 반환된다")
    void searchByNonExistentCountry() {
        // Given
        Country nonExistentCountry = new Country("XX", "존재하지않는국가");
        HolidaySearchCondition condition = HolidaySearchCondition.builder().country(nonExistentCountry).build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Holiday> result = holidayRepositoryCustom.searchByConditions(condition, pageable);

        // Then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("존재하지 않는 연도로 검색하면 빈 결과가 반환된다")
    void searchByNonExistentYear() {
        // Given
        HolidaySearchCondition condition = HolidaySearchCondition.builder().year(2025).build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Holiday> result = holidayRepositoryCustom.searchByConditions(condition, pageable);

        // Then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("존재하지 않는 휴일 타입으로 검색하면 빈 결과가 반환된다")
    void searchByNonExistentHolidayType() {
        // Given
        HolidaySearchCondition condition = HolidaySearchCondition.builder().type(HolidayType.AUTHORITIES).build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Holiday> result = holidayRepositoryCustom.searchByConditions(condition, pageable);

        // Then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("날짜 범위가 데이터 범위를 벗어나면 빈 결과가 반환된다")
    void searchByOutOfRangeDateRange() {
        // Given
        HolidaySearchCondition condition = HolidaySearchCondition.builder().startDate(LocalDate.of(2025, 1, 1)).endDate(
                LocalDate.of(2025, 12, 31)).build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Holiday> result = holidayRepositoryCustom.searchByConditions(condition, pageable);

        // Then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("정렬이 날짜 오름차순, 국가코드 오름차순으로 정상적으로 동작한다")
    void searchWithProperSorting() {
        // Given
        HolidaySearchCondition condition = HolidaySearchCondition.builder().year(2023).build();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Holiday> result = holidayRepositoryCustom.searchByConditions(condition, pageable);

        // Then
        assertThat(result.getContent()).hasSize(6);

        // 첫 번째와 두 번째 요소가 같은 날짜(1월 1일)인 경우 국가코드 순으로 정렬되는지 확인
        Holiday first = result.getContent().get(0);
        Holiday second = result.getContent().get(1);

        assertThat(first.getDate()).isEqualTo(LocalDate.of(2023, 1, 1));
        assertThat(second.getDate()).isEqualTo(LocalDate.of(2023, 1, 1));
        assertThat(first.getCountry().getCountryCode()).isEqualTo("KR");
        assertThat(second.getCountry().getCountryCode()).isEqualTo("US");

        // 전체적으로 날짜 오름차순인지 확인
        for (int i = 0; i < result.getContent().size() - 1; i++) {
            LocalDate currentDate = result.getContent().get(i).getDate();
            LocalDate nextDate = result.getContent().get(i + 1).getDate();
            assertThat(currentDate).isBeforeOrEqualTo(nextDate);
        }
    }
}
