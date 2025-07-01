package com.holidaymini.repository;

import com.holidaymini.domain.Holiday;
import com.holidaymini.repository.dto.HolidaySearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HolidayRepositoryCustom {

    Page<Holiday> searchByConditions(HolidaySearchCondition condition, Pageable pageable);
}
