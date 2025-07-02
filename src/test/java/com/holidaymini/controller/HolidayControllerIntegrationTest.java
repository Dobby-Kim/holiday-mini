package com.holidaymini.controller;

import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.holidaymini.controller.dto.HolidaySearchFilter;
import com.holidaymini.domain.Country;
import com.holidaymini.domain.Holiday;
import com.holidaymini.domain.HolidayDetail;
import com.holidaymini.domain.HolidayType;
import com.holidaymini.repository.CountryRepository;
import com.holidaymini.repository.HolidayRepository;
import io.restassured.RestAssured;
import java.time.LocalDate;
import java.util.EnumSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HolidayControllerIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    HolidayRepository holidayRepository;

    @BeforeEach
    void setUp() {
        holidayRepository.deleteAll();
        countryRepository.deleteAll();

        Country kr = countryRepository.save(new Country("KR", "Korea"));
        Country us = countryRepository.save(new Country("US", "United States"));

        // KR 2025 공휴일 2건
        HolidayDetail detail1 = new HolidayDetail(true, false, "설날", EnumSet.of(HolidayType.PUBLIC));
        HolidayDetail detail2 = new HolidayDetail(false, true, "추석", EnumSet.of(HolidayType.PUBLIC));
        HolidayDetail detail3 = new HolidayDetail(
                true,
                true,
                "도엽 탄신일",
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

        RestAssured.port = port;
    }

    @Test
    @DisplayName("조건 검색 1 - 국가 / 연도")
    void testFilterByCountryAndYear() {
        HolidaySearchFilter filter = new HolidaySearchFilter("KR", 2025, null, null, null);

        RestAssured.given().contentType(JSON).body(filter)
                .when().get("/api/holidays?page=0&size=10&sort=date,asc")
                .then().statusCode(200).body("content", hasSize(3)).body(
                        "content.name",
                        contains("Lunar New Year", "Dobby Birthday", "Chuseok")
                ).body("pageInfo.totalElements", equalTo(3));
    }

    @Test
    @DisplayName("조건 검색 2 - 국가 / 연도 / 기간")
    void testFilterByCountryAndYearAndDate() {
        HolidaySearchFilter filter = new HolidaySearchFilter(
                "KR",
                2025,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 3, 1),
                null
        );

        RestAssured.given().contentType(JSON).body(filter)
                .when().get("/api/holidays?page=0&size=10&sort=date,asc")
                .then().statusCode(200).body("content", hasSize(2)).body(
                        "content.name",
                        contains("Lunar New Year", "Dobby Birthday")
                ).body("pageInfo.totalElements", equalTo(2));
    }

    @Test
    @DisplayName("조건 검색 3 - 국가 / 연도 / 기간 / 카테고리")
    void testFilterByCountry_Year_Date_Category() {
        HolidaySearchFilter filter = new HolidaySearchFilter(
                "KR",
                2025,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 3, 1),
                HolidayType.AUTHORITIES
        );

        RestAssured.given().contentType(JSON).body(filter)
                .when().get("/api/holidays?page=0&size=10&sort=date,asc")
                .then().statusCode(200).body("content", hasSize(1)).body(
                        "content.name",
                        contains("Dobby Birthday")
                ).body("pageInfo.totalElements", equalTo(1));
    }

    @Test
    @DisplayName("조건 검색 4 - 국가 / 연도 / 카테고리")
    void testFilterByCountry_Year_Category() {
        HolidaySearchFilter filter = new HolidaySearchFilter(
                "KR",
                2025,
                null,
                null,
                HolidayType.AUTHORITIES
        );

        RestAssured.given().contentType(JSON).body(filter)
                .when().get("/api/holidays?page=0&size=10&sort=date,asc")
                .then().statusCode(200).body("content", hasSize(1)).body(
                        "content.name",
                        contains("Dobby Birthday")
                ).body("pageInfo.totalElements", equalTo(1));
    }

    @Test
    @DisplayName("정렬 옵션 변경 시 결과가 올바르게 정렬된다")
    void testSorting() {
        RestAssured.given().contentType(JSON).body(new HolidaySearchFilter("KR", 2025, null, null, null))
                .when().get("/api/holidays?page=0&size=10&sort=name,desc")
                .then().statusCode(200).body("content.name[0]", equalTo("Lunar New Year")).body(
                        "content.name[-1]",
                        equalTo("Chuseok")
                );
    }

    @Test
    @DisplayName("존재하지 않는 국가 코드로 검색시 400 에러를 반환한다")
    void testInvalidCountryCode() {
        HolidaySearchFilter filter = new HolidaySearchFilter("INVALID_CODE", 2025, null, null, null);

        RestAssured.given().contentType(JSON).body(filter)
                .when().get("/api/holidays?page=0&size=10")
                .then().statusCode(400);
    }

    @Test
    @DisplayName("요청에 국가 코드 미포함 시, 검색시 400 에러를 반환한다")
    void testInvalid_Empty_CountryCode() {
        HolidaySearchFilter filter = new HolidaySearchFilter(null, 2025, null, null, null);

        RestAssured.given().contentType(JSON).body(filter)
                .when().get("/api/holidays?page=0&size=10")
                .then().statusCode(400);
    }

    @Test
    @DisplayName("2020 - 2025년 범위를 벗어난 연도로 검색 시, 404 에러를 반환한다")
    void testInvalidYear() {
        HolidaySearchFilter filter = new HolidaySearchFilter("KR", 2099, null, null, null);

        RestAssured.given().contentType(JSON).body(filter)
                .when().get("/api/holidays?page=0&size=10")
                .then().statusCode(404);
    }

    @Test
    @DisplayName("요청에 연도 미포함 시, 검색시 400 에러를 반환한다")
    void testInvalid_Empty_Year() {
        HolidaySearchFilter filter = new HolidaySearchFilter("KR", null, null, null, null);

        RestAssured.given().contentType(JSON).body(filter)
                .when().get("/api/holidays?page=0&size=10")
                .then().statusCode(400);
    }
}
