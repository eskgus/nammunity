package com.eskgus.nammunity.web.dto.pagination;

import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class ContentsPageDto<Dto> {
    private final Page<Dto> contents;
    private PaginationDto<Dto> pages;

    public ContentsPageDto(Page<Dto> contents) {
        this.contents = contents;
        generatePages();
    }

    private void generatePages() {
        this.pages = PaginationDto.<Dto>builder().page(contents).display(10).build();
    }
}
