package com.eskgus.nammunity.web.dto.pagination;

import lombok.Getter;

@Getter
public class PageItem {
    private int page;
    private boolean isCurrentPage;

    public PageItem(int page, boolean isCurrentPage) {
        this.page = page;
        this.isCurrentPage = isCurrentPage;
    }
}
