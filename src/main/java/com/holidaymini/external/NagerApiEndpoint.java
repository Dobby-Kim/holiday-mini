package com.holidaymini.external;

import lombok.Getter;

@Getter
public enum NagerApiEndpoint {

    GET_AVAILABLE_COUNTRIES("/AvailableCountries"),
    GET_PUBLIC_HOLIDAYS("/PublicHolidays/{year}/{countryCode}")
    ;

    private final String url;

    NagerApiEndpoint(String url) {
        this.url = url;
    }
}
