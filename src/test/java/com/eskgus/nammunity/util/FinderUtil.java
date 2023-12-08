package com.eskgus.nammunity.util;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import org.assertj.core.api.Assertions;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class FinderUtil {
    @Getter
    public static class FindDto<T> {
        private User user;
        private Function<User, List<T>> finder; // find@@ByUser()
        private Function<T, User> userExtractor;    // getUser
        private Function<T, Long> idExtractor;  // getId
        private List<Long> expectedIdList;

        @Builder
        public FindDto(User user, Function<User, List<T>> finder, List<Long> expectedIdList) {
            this.user = user;
            this.finder = finder;
            this.userExtractor = this::extractUser;
            this.idExtractor = this::extractId;
            this.expectedIdList = expectedIdList;
        }

        private User extractUser(T t) {
            if (t instanceof Posts) {
                return ((Posts) t).getUser();
            } else if (t instanceof Comments) {
                return ((Comments) t).getUser();
            } else {
                return ((Likes) t).getUser();
            }
        }

        private Long extractId(T t) {
            if (t instanceof Posts) {
                return ((Posts) t).getId();
            } else if (t instanceof Comments) {
                return ((Comments) t).getId();
            } else {
                return ((Likes) t).getId();
            }
        }
    }

    public static <T> void callAndAssertFindContentsByUser(FindDto<T> findDto) {
        User user = findDto.getUser();
        List<Long> expectedIdList = findDto.getExpectedIdList();

        // 1. find@@ByUser() 메서드 호출
        List<T> result = findDto.getFinder().apply(user);
        Assertions.assertThat(result.size()).isEqualTo(expectedIdList.size());

        // 2. result의 user id가 user의 id랑 같은지 확인
        for (T t : result) {
            Assertions.assertThat(findDto.getUserExtractor().apply(t).getId()).isEqualTo(user.getId());
        }

        // 3. result의 id가 내림차순인지 확인
        expectedIdList.sort(Comparator.reverseOrder());
        Assertions.assertThat(result).extracting(findDto.getIdExtractor()::apply).isEqualTo(expectedIdList);
    }
}
