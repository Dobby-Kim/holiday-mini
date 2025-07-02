package com.holidaymini.controller;

import com.holidaymini.controller.dto.HolidayResponse;
import com.holidaymini.controller.dto.HolidaySearchFilter;
import com.holidaymini.controller.dto.PageResponse;
import com.holidaymini.domain.Holiday;
import com.holidaymini.service.HolidaySearchService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidaySearchService holidaySearchService;

    @GetMapping
    public PageResponse<HolidayResponse> searchHolidays(
            @RequestBody @Valid HolidaySearchFilter request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date,asc") String[] sort
    ) {
        Pageable pageable = createPageable(page, size, sort);
        Page<Holiday> holidayPage = holidaySearchService.searchHolidays(request, pageable);

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
}
