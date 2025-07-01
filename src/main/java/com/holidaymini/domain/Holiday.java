package com.holidaymini.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(indexes = {
        @Index(name = "idx_holiday_country_year", columnList = "country_id, holiday_year"),
        @Index(name = "idx_holiday_date", columnList = "holiday_date"),
        @Index(name = "idx_holiday_country_date", columnList = "country_id, holiday_date")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Holiday extends BaseTimeEntity {

    private static final LocalDate LOWER_DATE_BOUND = LocalDate.of(2020, 1, 1);
    private static final LocalDate UPPER_DATE_BOUND = LocalDate.of(2025, 12, 31);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holiday_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Column(nullable = false)
    private String name;

    @Column(name = "holiday_year", nullable = false)
    private Integer year;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate date;

    @Embedded
    private HolidayDetail detail;

    public Holiday(Country country, LocalDate date, String name, HolidayDetail detail) {
        validateDate(date);
        this.country = country;
        this.date = date;
        this.name = name;
        this.year = date.getYear();
        this.detail = detail;
    }

    private void validateDate(LocalDate date) {
        if (date.isBefore(LOWER_DATE_BOUND) || date.isAfter(UPPER_DATE_BOUND)) {
            throw new IllegalArgumentException("가능 연도: " + LOWER_DATE_BOUND + " - " + UPPER_DATE_BOUND);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Holiday holiday)) {
            return false;
        }
        return Objects.equals(id, holiday.id) && Objects.equals(country, holiday.country)
                && Objects.equals(year, holiday.year) && Objects.equals(date, holiday.date)
                && Objects.equals(name, holiday.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, country, year, date, name);
    }

    @Override
    public String toString() {
        return "Holiday{" +
                "id=" + id +
                ", date=" + date +
                ", name='" + name + '\'' +
                ", countryCode='" + country.getName() + '\'' +
                '}';
    }
}
