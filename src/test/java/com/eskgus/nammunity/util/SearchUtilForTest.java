package com.eskgus.nammunity.util;

import com.eskgus.nammunity.helper.SearchHelperForTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static com.eskgus.nammunity.util.PaginationUtilForTest.*;

public class SearchUtilForTest {    // U: entity, T: listDto
    private static SearchHelperForTest searchHelper;

    public static void initializeSearchHelper(SearchHelperForTest inputSearchHelper) {
        searchHelper = inputSearchHelper;
    }

    public static <V> void callAndAssertSearch() {
        Pageable pageable = createPageable(searchHelper.getPage(), searchHelper.getLimit());

        Page<V> actualSearchResult = searchHelper.applySearcher(pageable);
        Page<V> expectedSearchResult = createExpectedSearchResult(pageable);

        initializePaginationUtil(actualSearchResult, expectedSearchResult, searchHelper.getEntityConverter());
        assertActualPageEqualsExpectedPage();
    }

    private static <U, V> Page<V> createExpectedSearchResult(Pageable pageable) {
        Stream<U> keywordsFilter = searchHelper.getKeywordsFilter();

        List<V> dtos = keywordsFilter
                .sorted(Comparator.comparing(entity ->
                        searchHelper.getEntityConverter().extractEntityId(entity))
                        .reversed())
                .map(entity -> (V) searchHelper.getEntityConverter().generateListDto(entity))
                .toList();

        return createPage(dtos, pageable);
    }
}
