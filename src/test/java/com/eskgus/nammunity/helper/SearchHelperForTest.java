package com.eskgus.nammunity.helper;

import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.helper.repository.searcher.RepositoryBiSearcherForTest;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@Getter
public class SearchHelperForTest<T, U, V> {    // T: searcher<listDto>, U: entity, V: listDto
    private final T searcher;
    private final String keywords;
    private final KeywordHelper keywordHelper;

    private final List<U> totalContents;
    private final Function<U, String>[] fieldExtractors;
    private final String searchBy;

    private final int page;
    private final int limit;

    private final EntityConverterForTest<U, V> entityConverter;

    @Builder
    public SearchHelperForTest(T searcher, String keywords,
                               List<U> totalContents, Function<U, String>[] fieldExtractors, String searchBy,
                               int page, int limit, EntityConverterForTest<U, V> entityConverter) {
        this.searcher = searcher;
        this.keywords = keywords;
        this.keywordHelper = new KeywordHelper().extractKeywords(keywords);
        this.totalContents = totalContents;
        this.fieldExtractors = fieldExtractors;
        this.searchBy = searchBy;
        this.page = page;
        this.limit = limit;
        this.entityConverter = entityConverter;
    }

    public Page<V> applySearcher(Pageable pageable) {
        if (searcher instanceof RepositoryBiSearcherForTest) {
            return ((RepositoryBiSearcherForTest<V>) searcher).apply(keywords, pageable);
        }
        return null;
    }

    public Stream<U> getKeywordsFilter() {
        Stream<U> includeKeywordsFilter = filterIncludeKeywords();
        return filterExcludeKeywords(includeKeywordsFilter);
    }

    private Stream<U> filterIncludeKeywords() {
        return totalContents.stream().filter(content -> {
            boolean result1 = containsAny(fieldExtractors[0].apply(content));
            if (fieldExtractors.length > 1) {
                boolean result2 = containsAny(fieldExtractors[1].apply(content));
                return result1 || result2;
            }
            return result1;
        });
    }

    private boolean containsAny(String field) {
        return keywordHelper.getIncludeKeywordList().stream().anyMatch(field::contains);
    }

    private Stream<U> filterExcludeKeywords(Stream<U> includeKeywordsFilter) {
        if (keywordHelper.getExcludeKeywordList() == null) {
            return includeKeywordsFilter;
        }

        return includeKeywordsFilter.filter(content -> {
            boolean result1 = doesNotContainAny(fieldExtractors[0].apply(content));
            if (fieldExtractors.length > 1) {
                boolean result2 = doesNotContainAny(fieldExtractors[1].apply(content));
                return result1 && result2;
            }
            return result1;
        });
    }

    private boolean doesNotContainAny(String field) {
        return keywordHelper.getExcludeKeywordList().stream().noneMatch(field::contains);
    }
}
