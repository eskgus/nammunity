package com.eskgus.nammunity.helper;

import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.pagination.PaginationDto;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;

@Log4j2
public class FindHelperForTest2<T, U> { // T: dto, U: entity
    private final ContentsPageDto<T> actualResult;
    private final Page<T> expectedContents;
    private final EntityConverterForTest<U, T> entityConverter;

    @Builder
    public FindHelperForTest2(ContentsPageDto<T> actualResult, Page<T> expectedContents,
                              EntityConverterForTest<U, T> entityConverter) {
        log.info("FindHelperForTest2().....");
        this.actualResult = actualResult;
        this.expectedContents = expectedContents;
        this.entityConverter = entityConverter;
    }

    public void callAndAssertFind() {
        log.info("callAndAssertFind().....");
        PaginationDto<T> expectedPages = createExpectedPages();

        PaginationHelperForTest2<T, U> paginationHelper = PaginationHelperForTest2.<T, U>builder()
                .actualResult(actualResult).expectedContents(expectedContents).expectedPages(expectedPages)
                .entityConverter(entityConverter).build();
        paginationHelper.assertResults();
    }

    private PaginationDto<T> createExpectedPages() {
        log.info("createExpectedPages().....");
        return PaginationDto.<T>builder().page(expectedContents).display(10).build();
    }
}
