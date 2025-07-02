package com.holidaymini.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HolidayDetail {

    @Column(nullable = false)
    private String localName;

    @Column(nullable = false)
    private Boolean isFixed;

    @Column(nullable = false)
    private Boolean isGlobal;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "holiday_types", joinColumns = @JoinColumn(name = "holiday_id"))
    @Enumerated(EnumType.STRING)
    private Set<HolidayType> types;

    public HolidayDetail(
            Boolean isFixed,
            Boolean isGlobal,
            String localName,
            Set<HolidayType> types
    ) {
        this.isFixed = isFixed;
        this.isGlobal = isGlobal;
        this.localName = localName;
        this.types = types;
    }
}
