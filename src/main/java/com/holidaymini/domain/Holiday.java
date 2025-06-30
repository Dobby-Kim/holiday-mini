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
        @Index(name = "idx_holiday_date", columnList = "date"),
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

    /**
     * Constructs a new Holiday entity with the specified country, date, name, and detail.
     *
     * The year is automatically set based on the provided date.
     *
     * @param country the country associated with the holiday
     * @param date the date of the holiday
     * @param name the name of the holiday
     * @param detail additional details about the holiday
     */
    public Holiday(Country country, LocalDate date, String name, HolidayDetail detail) {
        this.country = country;
        this.date = date;
        this.name = name;
        this.year = date.getYear();
        this.detail = detail;
    }

    /**
     * Determines whether this holiday is equal to another object based on id, country, year, date, and name.
     *
     * @param o the object to compare with this holiday
     * @return true if the specified object is a Holiday with the same id, country, year, date, and name; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Holiday holiday)) {
            return false;
        }
        return Objects.equals(id, holiday.id) && Objects.equals(country, holiday.country)
                && Objects.equals(year, holiday.year) && Objects.equals(date, holiday.date)
                && Objects.equals(name, holiday.name);
    }

    /**
     * Returns a hash code value for this holiday based on its id, country, year, date, and name.
     *
     * @return the hash code value for this holiday
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, country, year, date, name);
    }

    /**
     * Returns a string representation of the Holiday, including its id, date, local name, name, country name, and year.
     *
     * @return a string summarizing the holiday's key attributes
     */
    @Override
    public String toString() {
        return "Holiday{" +
                "id=" + id +
                ", date=" + date +
                ", localName='" + detail.getLocalName() + '\'' +
                ", name='" + name + '\'' +
                ", countryCode='" + country.getName() + '\'' +
                ", year=" + year +
                '}';
    }
}
