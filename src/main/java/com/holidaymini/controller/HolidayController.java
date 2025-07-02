package com.holidaymini.controller;

import com.holidaymini.controller.dto.HolidayResponse;
import com.holidaymini.controller.dto.HolidaySearchFilter;
import com.holidaymini.controller.dto.PageResponse;
import com.holidaymini.domain.Holiday;
import com.holidaymini.service.HolidayService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayService holidayService;

    @PostMapping
    public PageResponse<HolidayResponse> searchHolidays(
            @RequestBody @Valid HolidaySearchFilter request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date,asc") String[] sort
    ) {
        Pageable pageable = createPageable(page, size, sort);
        Page<Holiday> holidayPage = holidayService.searchHolidays(request, pageable);

        List<HolidayResponse> content = holidayPage.getContent()
                .stream()
                .map(h -> new HolidayResponse(
                        h.getId(),
                        h.getName(),
                        h.getDetail().getLocalName(),
                        h.getDate(),
                        h.getYear(),
                        h.getCountry().getCountryCode(),
                        h.getCountry().getName(),
                        h.getDetail().getIsFixed(),
                        h.getDetail().getIsGlobal(),
                        h.getDetail().getTypes()
                ))
                .toList();

        return new PageResponse<>(content, holidayPage);
    }

    private Pageable createPageable(int page, int size, String[] sort) {
        Sort.Direction direction = Sort.Direction.ASC;
        String property = "date";

        if (sort.length > 0) {
            property = sort[0];
            if (sort.length > 1) {
                direction = Sort.Direction.fromString(sort[1]);
            }
        }

        return PageRequest.of(page, size, Sort.by(direction, property));
    }

    @PatchMapping
    public ResponseEntity<Void> upsert(
            @RequestParam @NotBlank(message = "국가 코드는 필수입니다") String countryCode,
            @RequestParam @NotNull(message = "연도는 필수입니다") @Min(2020) @Max(2025) Integer year) {
        holidayService.upsertByCountryCodeAndYear(countryCode, year);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<String> delete(String countryCode, Integer year) {
        holidayService.deleteByCountryCodeAndYear(countryCode, year);

        return ResponseEntity.noContent().build();
    }
}
