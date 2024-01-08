package com.eskgus.nammunity.util;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import lombok.Builder;
import lombok.Getter;
import org.assertj.core.api.Assertions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class FinderUtil {
    @Getter
    public static class FinderParams<T> {
        private int currentPage;
        private int limit;
        private BiFunction<User, Pageable, Page<T>> finder; // find@@ByUser()
        private User user;
        private int expectedTotalElements;
        private List<Long> expectedIdList;
        private Function<T, String> authorExtractor;   // getAuthor/getNickname
        private Function<T, Long> idExtractor;  // getId

        @Builder
        public FinderParams(int currentPage, int limit,
                            BiFunction<User, Pageable, Page<T>> finder, User user,
                            int expectedTotalElements, List<Long> expectedIdList) {
            this.currentPage = currentPage;
            this.limit = limit;
            this.finder = finder;
            this.user = user;
            this.expectedTotalElements = expectedTotalElements;
            this.expectedIdList = expectedIdList;
            this.authorExtractor = this::extractAuthor;
            this.idExtractor = this::extractId;
        }

        private String extractAuthor(T t) {
            if (t instanceof PostsListDto) {
                return ((PostsListDto) t).getAuthor();
            } else if (t instanceof CommentsListDto) {
                return ((CommentsListDto) t).getAuthor();
            } else {
                return ((LikesListDto) t).getUser().getNickname();
            }
        }

        private Long extractId(T t) {
            if (t instanceof PostsListDto) {
                return ((PostsListDto) t).getId();
            } else if (t instanceof CommentsListDto) {
                return ((CommentsListDto) t).getCommentsId();
            } else {
                return ((LikesListDto) t).getLikesId();
            }
        }
    }

    public static Pageable createPageable(int page, int limit) {
        return PageRequest.of(page - 1, limit);
    }

    public static <T> void assertPageForRepositoryTest(Page<T> page, long expectedTotalElements, FinderParams<T> finderParams) {
        // 1. totalElements, totalPages, numberOfElements 확인
        int expectedNumberOfElements = Math.min((int) expectedTotalElements, page.getSize());
        assertPage(page, expectedTotalElements, expectedNumberOfElements);

        // 2. 컨텐츠 목록 List<T>의 각 컨텐츠 id가 내림차순인지 확인
        Assertions.assertThat(page.getContent()).extracting(finderParams.getIdExtractor())
                .isEqualTo(finderParams.getExpectedIdList());
    }

    public static <T> void assertPageForServiceTest(Page<T> page, long expectedTotalElements) {
        // 1. totalElements, totalPages, numberOfElements 확인
        assertPage(page, expectedTotalElements, 1);
    }

    public static <T> void assertPage(Page<T> page, long expectedTotalElements, int expectedNumberOfElements) {
        // 1. totalElements(전체 컨텐츠(게시글/댓글/좋아요) 개수)가 expectedTotalElements인지 확인
        Assertions.assertThat(page.getTotalElements()).isEqualTo(expectedTotalElements);

        // 2. totalPages(전체 페이지 수)가 전체 컨텐츠 수 / limit (소수점 올림)인지 확인
        int expectedTotalPages = (int) Math.ceil((double) expectedTotalElements / page.getSize());
        Assertions.assertThat(page.getTotalPages()).isEqualTo(expectedTotalPages);

        // 3. numberOfElements(현재 페이지에 있는 컨텐츠 수)가 expectedNumberOfElements인지 확인
        Assertions.assertThat(page.getNumberOfElements()).isEqualTo(expectedNumberOfElements);
    }

    public static <T> void callAndAssertFindContentsByUser(FinderParams<T> finderParams) {
        // 1. find@@ByUser() 호출
        Pageable pageable = createPageable(finderParams.getCurrentPage(), finderParams.getLimit());
        Page<T> result = finderParams.getFinder().apply(finderParams.getUser(), pageable);

        // 2. expectedIdList 내림차순 정렬
        finderParams.getExpectedIdList().sort((Comparator.reverseOrder()));

        // 3. result 검증
        assertPageForRepositoryTest(result, finderParams.getExpectedTotalElements(), finderParams);

        // 4. 컨텐츠 목록 List<T>의 각 author/user nickname이 user의 nickname인지 확인
        for (T type : result.getContent()) {
            Assertions.assertThat(finderParams.getAuthorExtractor().apply(type))
                    .isEqualTo(finderParams.getUser().getNickname());
        }
    }
}
