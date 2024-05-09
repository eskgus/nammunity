package com.eskgus.nammunity.util;

import com.eskgus.nammunity.converter.EntityConverterForTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class PaginationTestUtil {   // T: dto, U: entity
    public static <T, U> Page<T> createPageWithContent(Stream<U> entityStream,
                                                       EntityConverterForTest<T, U> entityConverter,
                                                       Pageable pageable) {
        List<T> dtos = entityStream.sorted(Comparator.comparing(entityConverter::extractEntityId).reversed())
                .map(entityConverter::generateDto).toList();

        return createPage(dtos, pageable);
    }

    private static <T> Page<T> createPage(List<T> dtos, Pageable pageable) {
        int totalElements = dtos.size();

        int fromIndex = (int) pageable.getOffset();
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), totalElements);
        List<T> content = dtos.subList(fromIndex, toIndex);

        return new PageImpl<>(content, pageable, totalElements);
    }
}
