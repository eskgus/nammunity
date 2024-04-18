package com.eskgus.nammunity.web.dto.pagination;

import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class ContentsPageDto<T> {
    private final Page<T> contents;
    private PaginationDto<T> pages;

    public ContentsPageDto(Page<T> contents) {
        this.contents = contents;
        generatePages();
    }

    private void generatePages() {
        this.pages = PaginationDto.<T>builder().page(contents).display(10).build();
    }
}
