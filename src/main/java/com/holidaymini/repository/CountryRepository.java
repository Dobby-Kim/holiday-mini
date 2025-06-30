package com.holidaymini.repository;

import com.holidaymini.domain.Country;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends JpaRepository<Country, String> {

    /**
 * Retrieves a country entity by its country code.
 *
 * @param countryCode the unique code identifying the country
 * @return an {@code Optional} containing the matching {@code Country} if found, or empty if not present
 */
Optional<Country> findByCountryCode(String countryCode);

    /**
 * Checks if a country with the specified country code exists in the database.
 *
 * @param countryCode the country code to search for
 * @return true if a country with the given country code exists, false otherwise
 */
boolean existsByCountryCode(String countryCode);
}
