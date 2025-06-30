package com.holidaymini.repository;

import com.holidaymini.domain.Country;
import com.holidaymini.domain.Holiday;
import com.holidaymini.domain.HolidayType;
import com.holidaymini.domain.QHoliday;
import com.holidaymini.repository.dto.HolidaySearchCondition;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HolidayRepositoryCustomImpl implements HolidayRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * Retrieves a paginated list of holidays matching the specified search conditions.
     *
     * Applies optional filters for country, year, date range, and holiday type, and returns results ordered by date and country code.
     *
     * @param condition the search criteria for filtering holidays
     * @param pageable the pagination and sorting information
     * @return a page of holidays matching the given conditions
     */
    @Override
    public Page<Holiday> searchByConditions(HolidaySearchCondition condition, Pageable pageable) {
        QHoliday holiday = QHoliday.holiday;

        List<Holiday> content = queryFactory.selectFrom(holiday).where(
                countryCodeEq(condition.getCountry()),
                yearEq(condition.getYear()),
                dateGoe(condition.getStartDate()),
                dateLoe(condition.getEndDate()),
                typeContains(condition.getType())
        ).offset(pageable.getOffset()).limit(pageable.getPageSize()).orderBy(
                holiday.date.asc(),
                holiday.country.countryCode.asc()
        ).fetch();

        JPAQuery<Long> countQuery = queryFactory.select(holiday.count()).from(holiday).where(
                countryCodeEq(condition.getCountry()),
                yearEq(condition.getYear()),
                dateGoe(condition.getStartDate()),
                dateLoe(condition.getEndDate()),
                typeContains(condition.getType())
        );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * Creates a predicate to filter holidays by the specified country.
     *
     * @param country the country to filter by; if {@code null}, no country filter is applied
     * @return a BooleanExpression for country equality, or {@code null} if no filter is needed
     */
    private BooleanExpression countryCodeEq(Country country) {
        return country != null ? QHoliday.holiday.country.eq(country) : null;
    }

    /**
     * Creates a predicate to filter holidays by the specified year.
     *
     * @param year the year to filter by; if {@code null}, no filtering is applied
     * @return a BooleanExpression for year equality, or {@code null} if year is {@code null}
     */
    private BooleanExpression yearEq(Integer year) {
        return year != null ? QHoliday.holiday.year.eq(year) : null;
    }

    /**
     * Creates a predicate to filter holidays with a date greater than or equal to the specified start date.
     *
     * @param startDate the lower bound for the holiday date filter; if {@code null}, no filter is applied.
     * @return a BooleanExpression for the date filter, or {@code null} if {@code startDate} is {@code null}.
     */
    private BooleanExpression dateGoe(LocalDate startDate) {
        return startDate != null ? QHoliday.holiday.date.goe(startDate) : null;
    }

    /**
     * Creates a predicate to filter holidays with a date less than or equal to the specified end date.
     *
     * @param endDate the latest holiday date to include; if {@code null}, no upper date filter is applied
     * @return a BooleanExpression for the date filter, or {@code null} if endDate is {@code null}
     */
    private BooleanExpression dateLoe(LocalDate endDate) {
        return endDate != null ? QHoliday.holiday.date.loe(endDate) : null;
    }

    /**
     * Creates a predicate to filter holidays whose detail types contain the specified holiday type.
     *
     * @param type the holiday type to filter by; if {@code null}, no filtering is applied
     * @return a BooleanExpression for the filter, or {@code null} if the type is {@code null}
     */
    private BooleanExpression typeContains(HolidayType type) {
        return type != null ? QHoliday.holiday.detail.types.contains(type) : null;
    }
}
