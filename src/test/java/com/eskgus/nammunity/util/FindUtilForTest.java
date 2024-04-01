package com.eskgus.nammunity.util;

import com.eskgus.nammunity.helper.FindHelperForTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static com.eskgus.nammunity.util.PaginationUtilForTest.*;

public class FindUtilForTest { // U: entity, V: listDto
    private static FindHelperForTest findHelper;

    public static void initializeFindHelper(FindHelperForTest inputFindHelper) {
        findHelper = inputFindHelper;
    }

    public static <V> void callAndAssertFind() {
        Pageable pageable = createPageable(findHelper.getPage(), findHelper.getLimit());

        Page<V> actualFindResult = findHelper.applyFinder(pageable);
        Page<V> expectedFindResult = createExpectedFindResult(pageable);

        initializePaginationUtil(actualFindResult, expectedFindResult, findHelper.getEntityConverter());
        assertActualPageEqualsExpectedPage();
    }

    private static <U, V> Page<V> createExpectedFindResult(Pageable pageable) {
        Stream<U> entityStream = findHelper.getEntityStream();

        List<V> dtos = entityStream
                .sorted(Comparator.comparing(entity ->
                            findHelper.getEntityConverter().extractEntityId(entity))
                        .reversed())
                .map(entity -> (V) findHelper.getEntityConverter().generateListDto(entity))
                .toList();

        return createPage(dtos, pageable);
    }
}
