package com.eskgus.nammunity.helper;

import lombok.Builder;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class SearchTestHelper<U> {  // U: entity
    private final List<U> totalContents;
    private final Function<U, String>[] fieldExtractors;
    private final KeywordHelper keywordHelper;

    @Builder
    public SearchTestHelper(List<U> totalContents, String keywords, Function<U, String>[] fieldExtractors) {
        this.totalContents = totalContents;
        this.fieldExtractors = fieldExtractors;
        this.keywordHelper = new KeywordHelper().extractKeywords(keywords);
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
