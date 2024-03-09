package com.eskgus.nammunity.helper;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.user.UsersListDto;
import lombok.Builder;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class SearchHelperForTest<T, U> {    // T: listDto, U: entity
    private String keywords;
    private KeywordHelper keywordHelper;
    private Function<String, List<T>> searcher;
    private List<U> totalContents;
    private Function<U, String>[] fieldExtractors;
    private Function<U, T> listDtoConstructor;
    private Function<T, Long> listDtoIdExtractor;

    @Builder
    public SearchHelperForTest(String keywords, Function<String, List<T>> searcher,
                               List<U> totalContents, Function<U, String>[] fieldExtractors) {
        this.keywords = keywords;
        this.keywordHelper = new KeywordHelper().extractKeywords(keywords);
        this.searcher = searcher;
        this.totalContents = totalContents;
        this.fieldExtractors = fieldExtractors;
        this.listDtoConstructor = this::constructListDto;
        this.listDtoIdExtractor = this::extractListDtoId;
    }

    private T constructListDto(U entity) {
        if (entity instanceof Posts) {
            return (T) new PostsListDto((Posts) entity);
        } else if (entity instanceof Comments) {
            return (T) new CommentsListDto((Comments) entity);
        } else {
            return (T) new UsersListDto((User) entity);
        }
    }

    private Long extractListDtoId(T listDto) {
        if (listDto instanceof PostsListDto) {
            return ((PostsListDto) listDto).getId();
        } else if (listDto instanceof CommentsListDto) {
            return ((CommentsListDto) listDto).getCommentsId();
        } else {
            return ((UsersListDto) listDto).getId();
        }
    }

    public void callAndAssertSearchByField() {
        List<T> actualSearchResult = searcher.apply(keywords);
        List<T> expectedSearchResult = getExpectedSearchResult();

        assertActualSearchResultEqualsExpectedSearchResult(actualSearchResult, expectedSearchResult);
    }

    private List<T> getExpectedSearchResult() {
        Stream<U> includeKeywordsFilter = filterIncludeKeywords();
        Stream<U> excludeKeywordsFilter = filterExcludeKeywords(includeKeywordsFilter);

        return excludeKeywordsFilter.map(listDtoConstructor).toList();
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

    private void assertActualSearchResultEqualsExpectedSearchResult(List<T> actualSearchResult,
                                                                    List<T> expectedSearchResult) {
        assertThat(actualSearchResult.size()).isEqualTo(expectedSearchResult.size());

        for (int i = 0; i < actualSearchResult.size(); i++) {
            T actualContent = actualSearchResult.get(i);
            T expectedContent = expectedSearchResult.get(i);
            assertThat(listDtoIdExtractor.apply(actualContent)).isEqualTo(listDtoIdExtractor.apply(expectedContent));
        }
    }
}
