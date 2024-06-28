package com.eskgus.nammunity.helper;

import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import lombok.Builder;
import org.springframework.data.domain.Page;

public class ContentsPageDtoTestHelper<Dto, Entity> {
    private final ContentsPageDto<Dto> actualResult;
    private final Page<Dto> expectedContents;
    private final EntityConverterForTest<Dto, Entity> entityConverter;

    @Builder
    public ContentsPageDtoTestHelper(ContentsPageDto<Dto> actualResult, Page<Dto> expectedContents,
                                     EntityConverterForTest<Dto, Entity> entityConverter) {
        this.actualResult = actualResult;
        this.expectedContents = expectedContents;
        this.entityConverter = entityConverter;
    }

    public void createExpectedResultAndAssertContentsPage() {
        ContentsPageDto<Dto> expectedResult = new ContentsPageDto<>(expectedContents);

        PaginationTestHelper<Dto, Entity> paginationHelper =
                new PaginationTestHelper<>(actualResult, expectedResult, entityConverter);
        paginationHelper.assertContentsPage();
    }
}
