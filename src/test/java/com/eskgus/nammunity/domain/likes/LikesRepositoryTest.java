package com.eskgus.nammunity.domain.likes;

import com.eskgus.nammunity.TestDB;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.function.Function;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class LikesRepositoryTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private LikesRepository likesRepository;

    @BeforeEach
    public void setUp() {
        // 1. user1 회원가입
        User user1 = userRepository.findById(testDB.signUp(1L, Role.USER)).get();
        Assertions.assertThat(userRepository.count()).isOne();

        // 2. user1이 게시글 작성
        Long postId = testDB.savePosts(user1);
        Assertions.assertThat(postsRepository.count()).isOne();

        // 3. user1이 댓글 작성
        Long commentId = testDB.saveComments(postId, user1);
        Assertions.assertThat(commentsRepository.count()).isOne();

        // 4. user1이 게시글 좋아요 + 댓글 좋아요
        testDB.savePostLikes(postId, user1);
        Long latestLikeId = testDB.saveCommentLikes(commentId, user1);
        Assertions.assertThat(likesRepository.count()).isEqualTo(latestLikeId);
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void countByUser() {
        // 1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(1L).get();
        User user2 = userRepository.findById(testDB.signUp(2L, Role.USER)).get();

        // 2. user1이 게시글 작성
        // 3. user1이 댓글 작성
        // 4. user2가 좋아요 x 후 호출
        callAndAssertCountLikesByUser(user2, likesRepository::countByUser, 0);

        // 5. user1이 게시글 좋아요 + 댓글 좋아요 후 호출
        callAndAssertCountLikesByUser(user1, likesRepository::countByUser, likesRepository.count());
    }

    @Test
    public void countPostLikesByUser() {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성
        // 3. user1이 댓글 작성
        // 4. user1이 게시글 좋아요 + 댓글 좋아요 후 호출, 리턴 값이 1(게시글 좋아요 1개)인지 확인
        callAndAssertCountLikesByUser(user1, likesRepository::countPostLikesByUser, 1);
    }

    @Test
    public void countCommentLikesByUser() {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성
        // 3. user1이 댓글 작성
        // 4. user1이 게시글 좋아요 + 댓글 좋아요 후 호출, 리턴 값이 1(댓글 좋아요 1개)인지 확인
        callAndAssertCountLikesByUser(user1, likesRepository::countCommentLikesByUser, 1);
    }

    private void callAndAssertCountLikesByUser(User user, Function<User, Long> function, long expectedCount) {
        // 1. user로 function 호출
        long actualCount = function.apply(user);

        // 2. 리턴 값이 expectedCount랑 같은지 확인
        Assertions.assertThat(actualCount).isEqualTo(expectedCount);
    }
}
