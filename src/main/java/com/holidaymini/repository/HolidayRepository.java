package com.holidaymini.repository;

import com.holidaymini.domain.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayRepository extends JpaRepository<Holiday, Long>, HolidayRepositoryCustom {

}
