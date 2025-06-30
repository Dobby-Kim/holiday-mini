package com.holidaymini.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Country {

    @Id
    private String countryCode;

    private String name;

    /**
     * Determines whether this Country is equal to another object based on the country code.
     *
     * @param o the object to compare with this Country
     * @return true if the specified object is a Country with the same country code; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Country country = (Country) o;
        return Objects.equals(countryCode, country.countryCode);
    }

    /**
     * Returns a hash code value for the Country based on its country code.
     *
     * @return the hash code derived from the country code
     */
    @Override
    public int hashCode() {
        return Objects.hash(countryCode);
    }

    /**
     * Returns a string representation of the Country, including its country code and name.
     *
     * @return a string describing the country code and name
     */
    @Override
    public String toString() {
        return "Country{" +
                "countryCode='" + countryCode + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
