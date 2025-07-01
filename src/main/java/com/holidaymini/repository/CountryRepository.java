package com.holidaymini.repository;

import com.holidaymini.domain.Country;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends JpaRepository<Country, String> {

    Optional<Country> findByCountryCode(String countryCode);

    boolean existsByCountryCode(String countryCode);
}
