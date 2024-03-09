package com.eskgus.nammunity.util;

import com.eskgus.nammunity.helper.FindHelperForTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static com.eskgus.nammunity.util.PaginationUtilForTest.assertActualPageEqualsExpectedPage;
import static com.eskgus.nammunity.util.PaginationUtilForTest.initializePaginationUtil;

public class FindUtilForTest { // U: entity, V: listDto
    private static FindHelperForTest findHelper;

    public static void initializeFindHelper(FindHelperForTest inputFindHelper) {
        findHelper = inputFindHelper;
    }

    public static <V> void callAndAssertFind() {
        Pageable pageable = createPageable();

        Page<V> actualFindResult = findHelper.applyFinder(pageable);
        Page<V> expectedFindResult = createExpectedFindResult(pageable);

        initializePaginationUtil(actualFindResult, expectedFindResult, findHelper.getEntityConverter());
        assertActualPageEqualsExpectedPage();
    }

    private static Pageable createPageable() {
        return PageRequest.of(findHelper.getPage() - 1, findHelper.getLimit());
    }

    private static <U, V> Page<V> createExpectedFindResult(Pageable pageable) {
        Stream<U> entityStream = findHelper.getEntityStream();

        List<V> dtos = entityStream
                .sorted(Comparator.comparing(entity ->
                            findHelper.getEntityConverter().extractEntityId(entity))
                        .reversed())
                .map(entity -> (V) findHelper.getEntityConverter().generateListDto(entity))
                .toList();

        int totalElements = dtos.size();

        int fromIndex = (int) pageable.getOffset();
        int toIndex = Math.min((fromIndex + pageable.getPageSize()), totalElements);
        List<V> content = dtos.subList(fromIndex, toIndex);

        return new PageImpl<>(content, pageable, totalElements);
    }
}
