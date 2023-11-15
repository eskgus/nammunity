package com.eskgus.nammunity.domain.comments;

import com.eskgus.nammunity.TestDB;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CommentsRepositoryTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void countByUser() {
        // 1. user1 회원가입
        User user1 = userRepository.findById(testDB.signUp(1L, Role.USER)).get();
        Assertions.assertThat(userRepository.count()).isOne();

        // 2. user1이 게시글 작성
        Long postId = testDB.savePosts(user1);
        Assertions.assertThat(postsRepository.count()).isOne();

        // 3. 댓글 작성 x 후 호출
        callAndAssertCountByUser(user1);

        // 4. 댓글 1개 작성 후 호출
        // 4-1. user1이 댓글 작성
        testDB.saveComments(postId, user1);
        Assertions.assertThat(commentsRepository.count()).isOne();

        callAndAssertCountByUser(user1);
    }

    private void callAndAssertCountByUser(User user) {
        // 1. expectedCount에 현재 저장된 댓글 수 저장
        long expectedCount = commentsRepository.count();

        // 2. user로 countByUser() 호출하고 리턴 값 actualCount에 저장
        long actualCount = commentsRepository.countByUser(user);

        // 3. actualCount가 expectedCount랑 같은지 확인
        Assertions.assertThat(actualCount).isEqualTo(expectedCount);
    }
}
