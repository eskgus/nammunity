package com.eskgus.nammunity.util;

import com.eskgus.nammunity.converter.EntityConverterForTest;
import org.springframework.data.domain.Page;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PaginationUtilForTest {   // U: entity, V: listDto
    private static Page actualPage;
    private static Page expectedPage;

    private static EntityConverterForTest entityConverter;

    public static <U, V> void initializePaginationUtil(Page<V> inputActualPage,
                                                Page<V> inputExpectedPage,
                                                EntityConverterForTest<U, V> inputEntityConverter) {
        actualPage = inputActualPage;
        expectedPage = inputExpectedPage;
        entityConverter = inputEntityConverter;
    }

    public static <V> void initializePaginationUtil(Page<V> inputActualPage, Page<V> inputExpectedPage) {
        actualPage = inputActualPage;
        expectedPage = inputExpectedPage;
    }

    public static void assertActualPageEqualsExpectedPage() {
        assertPage();

        if (entityConverter != null) {
            assertContent();
        }
    }

    private static void assertPage() {
        assertThat(actualPage.getTotalElements()).isEqualTo(expectedPage.getTotalElements());
        assertThat(actualPage.getTotalPages()).isEqualTo(expectedPage.getTotalPages());
        assertThat(actualPage.getNumberOfElements()).isEqualTo(expectedPage.getNumberOfElements());
    }

    private static <V> void assertContent() {
        List<V> actualContent = actualPage.getContent();
        List<V> expectedContent = expectedPage.getContent();
        assertThat(actualContent.size()).isEqualTo(expectedContent.size());

        for (int i = 0; i < actualContent.size(); i++) {
            V actualListDto = actualContent.get(i);
            V expectedListDto = expectedContent.get(i);
            assertThat(entityConverter.extractListDtoId(actualListDto))
                    .isEqualTo(entityConverter.extractListDtoId(expectedListDto));
        }
    }
}
