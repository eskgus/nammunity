package com.eskgus.nammunity.util;

import com.eskgus.nammunity.converter.EntityConverterForTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PaginationUtilForTest {   // U: entity, V: listDto
    private static Page actualPage;
    private static Page expectedPage;

    private static EntityConverterForTest entityConverter;

    public static Pageable createPageable(int page, int size) {
        return PageRequest.of(page - 1, size);
    }

    public static Page createPage(List dtos, Pageable pageable) {
        int totalElements = dtos.size();

        int fromIndex = (int) pageable.getOffset();
        int toIndex = Math.min((fromIndex + pageable.getPageSize()), totalElements);
        List content = dtos.subList(fromIndex, toIndex);

        return new PageImpl<>(content, pageable, totalElements);
    }

    public static <U, V> void initializePaginationUtil(Page<V> inputActualPage,
                                                Page<V> inputExpectedPage,
                                                EntityConverterForTest<U, V> inputEntityConverter) {
        actualPage = inputActualPage;
        expectedPage = inputExpectedPage;
        entityConverter = inputEntityConverter;
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
            assertThat(entityConverter.extractDtoId(actualListDto))
                    .isEqualTo(entityConverter.extractDtoId(expectedListDto));
        }
    }
}
