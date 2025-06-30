package com.holidaymini.repository;

import com.holidaymini.domain.Holiday;
import com.holidaymini.repository.dto.HolidaySearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HolidayRepositoryCustom {

    /**
 * Retrieves a paginated list of holidays matching the specified search conditions.
 *
 * @param condition the criteria used to filter holidays
 * @param pageable pagination information for the result set
 * @return a page of holidays that satisfy the given search conditions
 */
Page<Holiday> searchByConditions(HolidaySearchCondition condition, Pageable pageable);
}
