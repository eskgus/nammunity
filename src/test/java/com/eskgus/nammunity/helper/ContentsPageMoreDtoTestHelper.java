package com.eskgus.nammunity.helper;

import com.eskgus.nammunity.converter.CommentsConverterForTest;
import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.converter.LikesConverterForTest;
import com.eskgus.nammunity.converter.PostsConverterForTest;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageMoreDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageMoreDtos;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public class ContentsPageMoreDtoTestHelper<T, U, V> {
    private final List<ContentsPageMoreDto<?>> actualResult;
    private final Page<?>[] expectedContents;
    private final List<EntityConverterForTest<?, ?>> entityConverters;

    public ContentsPageMoreDtoTestHelper(ContentsPageMoreDtos<T, U, V> actualContentsPageMoreDtos,
                                         Page<?>... expectedContents) {
        this.actualResult = generateActualResult(actualContentsPageMoreDtos);
        this.expectedContents = expectedContents;
        this.entityConverters = generateEntityConverters();
    }

    private List<ContentsPageMoreDto<?>> generateActualResult(ContentsPageMoreDtos<T, U, V> actualContentsPageMoreDtos) {
        List<ContentsPageMoreDto<?>> actualResult = new ArrayList<>();
        actualResult.add(actualContentsPageMoreDtos.getContentsPageMore1());
        actualResult.add(actualContentsPageMoreDtos.getContentsPageMore2());
        actualResult.add(actualContentsPageMoreDtos.getContentsPageMore3());
        return actualResult;
    }

    private List<EntityConverterForTest<?, ?>> generateEntityConverters() {
        List<EntityConverterForTest<?, ?>> entityConverters = new ArrayList<>();
        for (Page<?> expectedContent : expectedContents) {
            Class<?> genericType = expectedContent.getContent().get(0).getClass();
            if (genericType.equals(PostsListDto.class)) {
                entityConverters.add(new PostsConverterForTest());
            } else if (genericType.equals(CommentsListDto.class)) {
                entityConverters.add(new CommentsConverterForTest<>(CommentsListDto.class));
            } else {
                entityConverters.add(new LikesConverterForTest());
            }
        }
        return entityConverters;
    }

    public void createExpectedResultAndAssertContentsPageMore() {
        List<ContentsPageMoreDto<?>> expectedResult = createExpectedResult();

        for (int i = 0; i < actualResult.size(); i++) {
            PaginationTestHelper<?, ?> paginationHelper
                    = new PaginationTestHelper<>(actualResult.get(i), expectedResult.get(i), entityConverters.get(i));
            paginationHelper.assertContentsPageMore();
        }
    }

    private List<ContentsPageMoreDto<?>> createExpectedResult() {
        List<ContentsPageMoreDto<?>> expectedResult = new ArrayList<>();

        for (Page<?> expectedContent : expectedContents) {
            ContentsPageMoreDto<?> expectedContentsPageMore = new ContentsPageMoreDto<>(expectedContent);
            expectedResult.add(expectedContentsPageMore);
        }

        return expectedResult;
    }
}
