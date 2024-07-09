package com.eskgus.nammunity.helper;

import lombok.Builder;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class SearchTestHelper<Entity> {
    private final List<Entity> totalContents;
    private final Function<Entity, String>[] fieldExtractors;
    private final KeywordHelper keywordHelper;

    @Builder
    public SearchTestHelper(List<Entity> totalContents, String keywords, Function<Entity, String>[] fieldExtractors) {
        this.totalContents = totalContents;
        this.fieldExtractors = fieldExtractors;
        this.keywordHelper = new KeywordHelper().extractKeywords(keywords);
    }

    public Stream<Entity> getKeywordsFilter() {
        Stream<Entity> includeKeywordsFilter = filterIncludeKeywords();
        return filterExcludeKeywords(includeKeywordsFilter);
    }

    private Stream<Entity> filterIncludeKeywords() {
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

    private Stream<Entity> filterExcludeKeywords(Stream<Entity> includeKeywordsFilter) {
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
