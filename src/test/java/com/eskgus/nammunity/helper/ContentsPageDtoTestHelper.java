package com.eskgus.nammunity.helper;

import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import lombok.Builder;
import org.springframework.data.domain.Page;

public class ContentsPageDtoTestHelper<T, U> { // T: dto, U: entity
    private final ContentsPageDto<T> actualResult;
    private final Page<T> expectedContents;
    private final EntityConverterForTest<U, T> entityConverter;

    @Builder
    public ContentsPageDtoTestHelper(ContentsPageDto<T> actualResult, Page<T> expectedContents,
                                     EntityConverterForTest<U, T> entityConverter) {
        this.actualResult = actualResult;
        this.expectedContents = expectedContents;
        this.entityConverter = entityConverter;
    }

    public void createExpectedResultAndAssertContentsPage() {
        ContentsPageDto<T> expectedResult = new ContentsPageDto<>(expectedContents);

        PaginationTestHelper<T, U> paginationHelper =
                new PaginationTestHelper<>(actualResult, expectedResult, entityConverter);
        paginationHelper.assertContentsPage();
    }
}
