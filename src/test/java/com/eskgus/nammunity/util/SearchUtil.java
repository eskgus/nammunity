package com.eskgus.nammunity.util;

import lombok.Builder;
import lombok.Getter;
import org.assertj.core.api.Assertions;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class SearchUtil {
    @Getter
    public static class SearchDto<T> {
        private List<T> contents;
        private Function<T, String> fieldExtractor;  // getTitle/getContent
        private Function<T, String> contentExtractor;  // getContent
        private Function<T, Long> idExtractor;  // getId
        private String[] includeKeywords;
        private String[] excludeKeywords;

        @Builder
        public SearchDto(List<T> contents, Function<T, String> fieldExtractor, Function<T, String> contentExtractor,
                         Function<T, Long> idExtractor, String[] includeKeywords, String[] excludeKeywords) {
            this.contents = contents;
            this.fieldExtractor = fieldExtractor;
            this.contentExtractor = contentExtractor;
            this.idExtractor = idExtractor;
            this.includeKeywords = includeKeywords;
            this.excludeKeywords = excludeKeywords;
        }
    }

    public static <T> List<Long> getExpectedIdList(SearchDto<T> searchDto) {
        // title/content/nickname에 includeKeywords 있는지 확인
        Stream<T> filteredContents = searchDto.getContents().stream()
                .filter(content -> {
                    boolean result1 = containsAny(content, searchDto.getFieldExtractor(), searchDto.getIncludeKeywords());

                    // 함수형 인터페이스가 2개(getTitle, getContent) 들어왔으면 두 결과를 or로 계산하고 리턴
                    if (searchDto.getContentExtractor() != null) {
                        boolean result2 = containsAny(content, searchDto.getContentExtractor(), searchDto.getIncludeKeywords());
                        return result1 || result2;
                    }
                    // 함수형 인터페이스가 1개(getTitle/getContent/getNickname) 들어왔으면 결과 바로 리턴
                    return result1;
                });

        // 매개변수로 excludeKeywords 들어왔으면 title/content/nickname에 excludeKeywords 있는지 확인
        if (searchDto.getExcludeKeywords() != null) {
            filteredContents = filteredContents
                    .filter(content -> {
                        boolean result1 = doesNotContainsAny(content, searchDto.getFieldExtractor(), searchDto.getExcludeKeywords());

                        // 함수형 인터페이스가 2개(getTitle, getContent) 들어왔으면 두 결과를 and로 계산하고 리턴
                        if (searchDto.getContentExtractor() != null) {
                            boolean result2 = doesNotContainsAny(content, searchDto.getContentExtractor(), searchDto.getExcludeKeywords());
                            return result1 && result2;
                        }
                        // 함수현 인터페이스가 1개(getTitle/getContent/getNickname) 들어왔으면 결과 바로 리턴
                        return result1;
                    });
        }

        // 걸러진 contents(posts/comments/users)에서 id만 뽑고, 내림차순 정렬 후 리스트로 반환
        return filteredContents.map(searchDto.getIdExtractor()).sorted(Comparator.reverseOrder()).toList();
    }

    public static <T> boolean containsAny(T content, Function<T, String> fieldExtractor, String... includeKeywords) {
        // fieldExtractor: getTitle(), getContent(), getNickname()
        return Arrays.stream(includeKeywords).anyMatch(keyword -> fieldExtractor.apply(content).contains(keyword));
    }

    public static <T> boolean doesNotContainsAny(T content, Function<T, String> fieldExtractor, String... excludeKeywords) {
        // fieldExtractor: getTitle(), getContent(), getNickname()
        return Arrays.stream(excludeKeywords).noneMatch(keyword -> fieldExtractor.apply(content).contains(keyword));
    }

    public static <T> void callAndAssertSearchByField(String keywords, Function<String, List<T>> function,
                                                      Function<T, Long> idExtractor, List<Long> expectedIdList) {
        // 1. searchBy@@() 메서드 호출
        List<T> result = function.apply(keywords);

        // 2. result의 id가 expectedIdList의 id랑 같은지 확인
        Assertions.assertThat(result).extracting(idExtractor).isEqualTo(expectedIdList);
    }
}
