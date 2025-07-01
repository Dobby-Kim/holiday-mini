package com.holidaymini.external.nager;

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
