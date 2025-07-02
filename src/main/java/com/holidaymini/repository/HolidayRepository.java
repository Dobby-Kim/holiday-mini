package com.holidaymini.repository;

import com.holidaymini.domain.Country;
import com.holidaymini.domain.Holiday;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayRepository extends JpaRepository<Holiday, Long>, HolidayRepositoryCustom {

    List<Holiday> findByCountryAndYear(Country country, Integer year);
}
