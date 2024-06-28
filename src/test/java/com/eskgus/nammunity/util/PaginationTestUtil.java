package com.eskgus.nammunity.util;

import com.eskgus.nammunity.converter.EntityConverterForTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class PaginationTestUtil {
    public static <Dto, Entity> Page<Dto> createPageWithContent(Stream<Entity> entityStream,
                                                                EntityConverterForTest<Dto, Entity> entityConverter,
                                                                Pageable pageable) {
        List<Dto> dtos = entityStream.sorted(Comparator.comparing(entityConverter::extractEntityId).reversed())
                .map(entityConverter::generateDto).toList();

        return createPage(dtos, pageable);
    }

    private static <Dto> Page<Dto> createPage(List<Dto> dtos, Pageable pageable) {
        int totalElements = dtos.size();

        int fromIndex = (int) pageable.getOffset();
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), totalElements);
        List<Dto> content = dtos.subList(fromIndex, toIndex);

        return new PageImpl<>(content, pageable, totalElements);
    }
}
