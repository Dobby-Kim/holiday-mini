package com.holidaymini.controller.dto;

import java.util.List;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class PageResponse<T> {

    private final List<T> content;
    private final PageInfo pageInfo;

    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.pageInfo = new PageInfo(page);
    }

    public PageResponse(List<T> content, Page<?> page) {
        this.content = content;
        this.pageInfo = new PageInfo(page);
    }

    @Getter
    public static class PageInfo {

        private final long totalElements;
        private final int totalPages;
        private final int currentPage;
        private final int pageSize;
        private final boolean hasNext;
        private final boolean hasPrevious;

        public PageInfo(Page<?> page) {
            this.totalElements = page.getTotalElements();
            this.totalPages = page.getTotalPages();
            this.currentPage = page.getNumber();
            this.pageSize = page.getSize();
            this.hasNext = page.hasNext();
            this.hasPrevious = page.hasPrevious();
        }
    }
}
