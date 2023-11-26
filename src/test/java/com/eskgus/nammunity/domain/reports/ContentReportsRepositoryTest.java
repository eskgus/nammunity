package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.util.TestDB;
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
public class ContentReportsRepositoryTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private ContentReportsRepository contentReportsRepository;

    @BeforeEach
    public void setUp() {
        // 1. user1 회원가입 + user2 회원가입
        testDB.signUp(1L, Role.USER);
        testDB.signUp(2L, Role.USER);
        Assertions.assertThat(userRepository.count()).isEqualTo(2);
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void countPostReportsByUser() {
        // 1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(1L).get();
        User user2 = userRepository.findById(2L).get();

        // 2. user1이 게시글 작성
        Long postId1 = testDB.savePosts(user1);
        Assertions.assertThat(postsRepository.count()).isOne();

        // 3. 게시글 신고 x 후 호출
        callAndAssertCountReportsByUser(user1, contentReportsRepository::countPostReportsByUser);

        // 4. user2가 user1이 작성한 게시글 신고 * 10 후 호출
        testDB.savePostReports(postId1, user2);
        Assertions.assertThat(contentReportsRepository.count()).isEqualTo(10);

        callAndAssertCountReportsByUser(user1, contentReportsRepository::countPostReportsByUser);
    }

    @Test
    public void countCommentReportsByUser() {
        // 1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(1L).get();
        User user2 = userRepository.findById(2L).get();

        // 2. user1이 게시글 작성
        Long postId = testDB.savePosts(user1);
        Assertions.assertThat(postsRepository.count()).isOne();

        // 3. user1이 댓글 작성
        Long commentId1 = testDB.saveComments(postId, user1);
        Assertions.assertThat(commentsRepository.count()).isOne();

        // 4. 댓글 신고 x 후 호출
        callAndAssertCountReportsByUser(user1, contentReportsRepository::countCommentReportsByUser);

        // 5. user2가 user1이 작성한 댓글 신고 * 10 후 호출
        testDB.saveCommentReports(commentId1, user2);
        Assertions.assertThat(contentReportsRepository.count()).isEqualTo(10);

        callAndAssertCountReportsByUser(user1, contentReportsRepository::countCommentReportsByUser);
    }

    @Test
    public void countUserReportsByUser() {
        // 1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(1L).get();
        User user2 = userRepository.findById(2L).get();

        // 2. 사용자 신고 x 후 호출
        callAndAssertCountReportsByUser(user1, contentReportsRepository::countUserReportsByUser);

        // 2. user2가 user1 사용자 신고 * 3 후 호출
        testDB.saveUserReports(user1, user2);
        Assertions.assertThat(contentReportsRepository.count()).isEqualTo(3);

        callAndAssertCountReportsByUser(user1, contentReportsRepository::countUserReportsByUser);
    }

    private void callAndAssertCountReportsByUser(User user, Function<User, Long> function) {
        // 1. expectedCount에 현재 저장된 (게시글/댓글/사용자) 신고 수 저장
        long expectedCount = contentReportsRepository.count();

        // 2. user로 function 호출
        long actualCount = function.apply(user);

        // 3. 리턴 값이 expectedCount랑 같은지 확인
        Assertions.assertThat(actualCount).isEqualTo(expectedCount);
    }
}
