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

    private BooleanExpression countryCodeEq(Country country) {
        return country != null ? QHoliday.holiday.country.eq(country) : null;
    }

    private BooleanExpression yearEq(Integer year) {
        return year != null ? QHoliday.holiday.year.eq(year) : null;
    }

    private BooleanExpression dateGoe(LocalDate startDate) {
        return startDate != null ? QHoliday.holiday.date.goe(startDate) : null;
    }

    private BooleanExpression dateLoe(LocalDate endDate) {
        return endDate != null ? QHoliday.holiday.date.loe(endDate) : null;
    }

    private BooleanExpression typeContains(HolidayType type) {
        return type != null ? QHoliday.holiday.detail.types.contains(type) : null;
    }
}
