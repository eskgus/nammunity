package com.eskgus.nammunity.web.dto.pagination;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PaginationDto<T> {
    private boolean hasNext = false;    // 다음/마지막 페이지 버튼 표시 여부
    private int lastPage;  // 마지막 페이지
    private int nextPage;   // 다음 페이지

    private boolean hasPrevious = false;    // 이전/처음 페이지 버튼 표시 여부
    private final int firstPage = 1;    // 처음 페이지
    private int previousPage;   // 이전 페이지

    private List<PageItem> displayPages;    // 페이지 번호 목록

    @Builder
    public PaginationDto(Page<T> page, int display) {
        this.lastPage = page.getTotalPages() > 0 ? page.getTotalPages() : 1;

        // displayPages, hasNext, nextPage, hasPrevious, previousPage 초기화
        initializeDisplayPages(page.getNumber() + 1, display);
    }

    public void initializeDisplayPages(int currentPage, int display) {
        // startPage, endPage 설정
        boolean isMultiple = currentPage % display == 0;
        int startPage = isMultiple ? display * (currentPage / display - 1) + 1 : display * (currentPage / display) + 1;
        int endPage = isMultiple ? display * (currentPage / display) : Math.min(display * (currentPage / display + 1), this.lastPage);

        // 한 번에 표시할 페이지 번호 설정
        List<PageItem> displayPages = new ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            boolean isCurrentPage = (i == currentPage);
            displayPages.add(new PageItem(i, isCurrentPage));
        }
        this.displayPages = displayPages;

        // 페이지 넘김 버튼 설정
        if ((startPage >= this.firstPage) && (endPage != this.lastPage)) {
            this.hasNext = true;
            this.nextPage = endPage + 1;
        }
        if (startPage > this.firstPage) {
            this.hasPrevious = true;
            this.previousPage = startPage - 1;
        }
    }
}
