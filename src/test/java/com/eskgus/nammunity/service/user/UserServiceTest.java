package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.reports.ContentReportsRepository;
import com.eskgus.nammunity.domain.user.BannedUsersRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.user.ActivityHistoryDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Period;

import static com.eskgus.nammunity.util.FinderUtil.assertPageForServiceTest;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserServiceTest {
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

    @Autowired
    private BannedUsersRepository bannedUsersRepository;

    @Autowired
    private UserService userService;

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void findActivityHistory() {
        // 1. user1 회원가입 + user2 (관리자) 회원가입
        User user1 = userRepository.findById(testDB.signUp(1L, Role.USER)).get();
        User user2 = userRepository.findById(testDB.signUp(2L, Role.ADMIN)).get();

        // 2. user1이 게시글 작성 * 11 + 댓글 작성 * 11
        for (int i = 0; i < 11; i++) {
            Long postId = testDB.savePosts(user1);
            testDB.saveComments(postId, user1);
        }

        // 3. user2가 user1 사용자 신고 * 3
        testDB.saveUserReports(user1, user2);
        Assertions.assertThat(contentReportsRepository.count()).isEqualTo(3);

        // 4. user1 활동 정지
        testDB.saveBannedUsers(user1, Period.ofWeeks(1));
        Assertions.assertThat(bannedUsersRepository.count()).isOne();

        // 5. type을 "posts"로 해서 findActivityHistory() 호출
        callAndAssertFindActivityHistory(user1, "posts");

        // 6. type을 "comments"로 해서 findActivityHistory() 호출
        callAndAssertFindActivityHistory(user1, "comments");
    }

    private void callAndAssertFindActivityHistory(User user, String type) {
        // 1. user id, type, page = 2로 findActivityHistory() 호출
        ActivityHistoryDto activityHistoryDto = userService.findActivityHistory(user.getId(), type, 2);

        // 2. activityHistoryDto 검증
        long expectedNumOfPosts = postsRepository.countByUser(user);
        long expectedNumOfComments = commentsRepository.countByUser(user);
        long expectedNumOfPostReports = contentReportsRepository.countPostReportsByUser(user);
        long expectedNumOfCommentReports = contentReportsRepository.countCommentReportsByUser(user);
        long expectedNumOfUserReports = contentReportsRepository.countUserReportsByUser(user);

        int expectedCount = bannedUsersRepository.findByUser(user).get().getCount();

        // 2-1. user 확인
        Assertions.assertThat(activityHistoryDto.getUserId()).isEqualTo(user.getId());

        // 2-2. 컨텐츠 개수 확인
        Assertions.assertThat(activityHistoryDto.getNumOfPosts()).isEqualTo(expectedNumOfPosts);
        Assertions.assertThat(activityHistoryDto.getNumOfComments()).isEqualTo(expectedNumOfComments);
        Assertions.assertThat(activityHistoryDto.getNumOfPostReports()).isEqualTo(expectedNumOfPostReports);
        Assertions.assertThat(activityHistoryDto.getNumOfCommentReports()).isEqualTo(expectedNumOfCommentReports);
        Assertions.assertThat(activityHistoryDto.getNumOfUserReports()).isEqualTo(expectedNumOfUserReports);

        // 2-3. 활동 정지 확인
        Assertions.assertThat(activityHistoryDto.getBannedUsersExistence()).isTrue();
        Assertions.assertThat(activityHistoryDto.getCount()).isEqualTo(expectedCount);

        // 2-4. 컨텐츠 Page<~ListDto> 확인
        if (type.equals("posts")) {
            assertPageForServiceTest(activityHistoryDto.getPosts(), expectedNumOfPosts);
        } else {
            assertPageForServiceTest(activityHistoryDto.getComments(), expectedNumOfComments);
        }
    }
}
