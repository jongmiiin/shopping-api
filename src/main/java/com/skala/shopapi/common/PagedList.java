package com.skala.shopapi.common;

import java.util.List;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class PagedList<T> {

    private final List<T> content;
    private final int totalPages;
    private final long totalElements;
    private final int offset;
    private final int count;

    private PagedList(List<T> content, int totalPages, long totalElements, int offset, int count) {
        this.content = content;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.offset = offset;
        this.count = count;
    }

    public static <T> PagedList<T> of(Page<T> page, int offset, int count) {
        return new PagedList<>(page.getContent(), page.getTotalPages(), page.getTotalElements(), offset, count);
    }
}
