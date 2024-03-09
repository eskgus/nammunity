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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Period;
import java.util.function.Function;

import static com.eskgus.nammunity.util.PaginationUtilForTest.assertActualPageEqualsExpectedPage;
import static com.eskgus.nammunity.util.PaginationUtilForTest.initializePaginationUtil;
import static org.assertj.core.api.Assertions.assertThat;

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

    private User[] users;
    private Pageable pageable;
    private ActivityHistoryDto activityHistoryDto;

    @BeforeEach
    public void setUp() {
        Long user1Id = testDB.signUp(1L, Role.USER);
        Long user2Id = testDB.signUp(2L, Role.ADMIN);
        assertThat(userRepository.count()).isEqualTo(user2Id);

        User user1 = userRepository.findById(user1Id).get();
        User user2 = userRepository.findById(user2Id).get();

        this.users = new User[]{ user1, user2 };
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void findActivityHistory() {
        savePostsAndComments();
        reportUser();
        banUser();

        int page = 2;
        this.pageable = createPageable(page);

        // 1. type = "posts"
        callAndAssertFindActivityHistory("posts",  page);

        // 2. type = "comments"
        callAndAssertFindActivityHistory("comments", page);
    }

    private void savePostsAndComments() {
        int numberOfContents = 11;
        for (int i = 0; i < numberOfContents; i++) {
            Long postId = testDB.savePosts(users[0]);
            testDB.saveComments(postId, users[0]);
        }
        assertThat(postsRepository.count()).isEqualTo(numberOfContents);
        assertThat(commentsRepository.count()).isEqualTo(numberOfContents);
    }

    private void reportUser() {
        Long latestUserReportId = testDB.saveUserReports(users[0], users[1]);
        assertThat(contentReportsRepository.count()).isEqualTo(latestUserReportId);
    }

    private void banUser() {
        Long bannedUserId = testDB.saveBannedUsers(users[0], Period.ofWeeks(1));
        assertThat(bannedUsersRepository.count()).isEqualTo(bannedUserId);
    }

    private Pageable createPageable(int page) {
        return PageRequest.of(page - 1, 10);
    }

    private void callAndAssertFindActivityHistory(String type, int page) {
        this.activityHistoryDto = userService.findActivityHistory(users[0].getId(), type, page);
        assertActivityHistoryDto(type);
    }

    private void assertActivityHistoryDto(String type) {
        assertThat(activityHistoryDto.getUserId()).isEqualTo(users[0].getId());
        assertNumberOfContents();
        assertUserBan();
        assertPages(type);
    }

    private void assertNumberOfContents() {
        assertNumberOfContent(activityHistoryDto.getNumOfPosts(), postsRepository::countByUser);
        assertNumberOfContent(activityHistoryDto.getNumOfComments(), commentsRepository::countByUser);
        assertNumberOfContent(activityHistoryDto.getNumOfPostReports(), contentReportsRepository::countPostReportsByUser);
        assertNumberOfContent(activityHistoryDto.getNumOfCommentReports(), contentReportsRepository::countCommentReportsByUser);
        assertNumberOfContent(activityHistoryDto.getNumOfUserReports(), contentReportsRepository::countUserReportsByUser);
    }

    private void assertNumberOfContent(long actualNumberOfContent, Function<User, Long> expectedCounter) {
        assertThat(actualNumberOfContent).isEqualTo(expectedCounter.apply(users[0]));
    }

    private void assertUserBan() {
        assertThat(activityHistoryDto.getBannedUsersExistence()).isTrue();

        int expectedCount = bannedUsersRepository.findByUser(users[0]).get().getCount();
        assertThat(activityHistoryDto.getCount()).isEqualTo(expectedCount);
    }

    private <V> void assertPages(String type) {
        Page<V> actualPage;
        Page<V> expectedPage;
        if (type.equals("posts")) {
            actualPage = (Page<V>) activityHistoryDto.getPosts();
            expectedPage = (Page<V>) postsRepository.findByUser(users[0], pageable);
        } else {
            actualPage = (Page<V>) activityHistoryDto.getComments();
            expectedPage = (Page<V>) commentsRepository.findByUser(users[0], pageable);
        }
        initializePaginationUtil(actualPage, expectedPage);
        assertActualPageEqualsExpectedPage();
    }
}
