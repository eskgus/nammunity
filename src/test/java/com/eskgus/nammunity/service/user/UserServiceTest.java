package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.converter.UserConverterForTest;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.reports.ContentReportSummaryRepository;
import com.eskgus.nammunity.helper.SearchHelperForTest;
import com.eskgus.nammunity.helper.repository.searcher.ServiceTriSearcherForTest;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.reports.ContentReportsRepository;
import com.eskgus.nammunity.domain.user.BannedUsersRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.user.*;
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
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.eskgus.nammunity.util.PaginationUtilForTest.assertActualPageEqualsExpectedPage;
import static com.eskgus.nammunity.util.PaginationUtilForTest.initializePaginationUtil;
import static com.eskgus.nammunity.util.SearchUtilForTest.callAndAssertSearch;
import static com.eskgus.nammunity.util.SearchUtilForTest.initializeSearchHelper;
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
    private ContentReportSummaryRepository reportSummaryRepository;

    @Autowired
    private UserService userService;

    private User[] users;
    private Pageable pageable;
    private ActivityHistoryDto activityHistoryDto;
    private final ContentType postType = ContentType.POSTS;
    private final ContentType commentType = ContentType.COMMENTS;
    private final ContentType userType = ContentType.USERS;

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
        saveUserReportSummary();
        banUser();

        int page = 2;
        this.pageable = createPageable(page);

        // 1. type = "posts"
        callAndAssertFindActivityHistory(postType.getDetailInEng(), page);

        // 2. type = "comments"
        callAndAssertFindActivityHistory(commentType.getDetailInEng(), page);
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

    private void saveUserReportSummary() {
        Long reportSummaryId = testDB.saveUserReportSummary(users[0], users[1]);
        assertThat(reportSummaryRepository.count()).isEqualTo(reportSummaryId);
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
        assertThat(activityHistoryDto.getUsersListDto().getId()).isEqualTo(users[0].getId());
        assertNumberOfContents(type);
        assertNumberOfReports();
        assertUserBan(activityHistoryDto.getBannedHistoryDto());
        assertPages(type);
    }

    private void assertNumberOfContents(String type) {
        Long actualNumberOfPosts;
        Long actualNumberOfComments;
        if (type.equals(ContentType.POSTS.getDetailInEng())) {
            PostsHistoryDto postsHistoryDto = activityHistoryDto.getPostsHistoryDto();
            actualNumberOfPosts = postsHistoryDto.getNumberOfPosts();
            actualNumberOfComments = postsHistoryDto.getNumberOfComments();
        } else {
            CommentsHistoryDto commentsHistoryDto = activityHistoryDto.getCommentsHistoryDto();
            actualNumberOfPosts = commentsHistoryDto.getNumberOfPosts();
            actualNumberOfComments = commentsHistoryDto.getNumberOfComments();
        }

        assertNumberOfContent(actualNumberOfPosts, postsRepository::countByUser);
        assertNumberOfContent(actualNumberOfComments, commentsRepository::countByUser);
    }

    private void assertNumberOfContent(long actualNumberOfContent, Function<User, Long> expectedCounter) {
        assertThat(actualNumberOfContent).isEqualTo(expectedCounter.apply(users[0]));
    }

    private void assertNumberOfReports() {
        Set<Map.Entry<String, Long>> numberOfReports = activityHistoryDto.getNumberOfReports();
        ContentType[] contentTypes = { postType, commentType, userType };
        int i = 0;
        for (Map.Entry<String, Long> numberOfReport : numberOfReports) {
            assertNumberOfReport(numberOfReport.getValue(), contentTypes[i]);
            i++;
        }
    }

    private void assertNumberOfReport(long actualNumberOfReport, ContentType contentType) {
        long expectedNumberOfReport = contentReportsRepository.countReportsByContentTypeAndUser(contentType, users[0]);
        assertThat(actualNumberOfReport).isEqualTo(expectedNumberOfReport);
    }

    private void assertUserBan(BannedHistoryDto bannedHistoryDto) {
        int expectedCount = bannedUsersRepository.findByUser(users[0]).get().getCount();
        assertThat(bannedHistoryDto.getCount()).isEqualTo(expectedCount);
    }

    private <V> void assertPages(String type) {
        Page<V> actualPage;
        Page<V> expectedPage;
        if (type.equals(postType.getDetailInEng())) {
            actualPage = (Page<V>) activityHistoryDto.getPostsHistoryDto().getPosts();
            expectedPage = (Page<V>) postsRepository.findByUser(users[0], pageable);
        } else {
            actualPage = (Page<V>) activityHistoryDto.getCommentsHistoryDto().getComments();
            expectedPage = (Page<V>) commentsRepository.findByUser(users[0], pageable);
        }
        initializePaginationUtil(actualPage, expectedPage, null);
        assertActualPageEqualsExpectedPage();
    }

    @Test
    public void searchByNickname() {
        signUpUsers();

        // 1. 검색 제외 단어 x
        callAndAssertSearchUsers("nick 네임", User::getNickname);

        // 2. 검색 제외 단어 o
        callAndAssertSearchUsers("nick 네임 -name", User::getNickname);
    }

    private void signUpUsers() {
        Long userId = testDB.signUp(users[1].getId() + 1, Role.USER);
        Long numberOfUsers = userId + 3;
        for (long i = userId; i < numberOfUsers; i++) {
            testDB.signUp("닉네임" + i, i + 1, Role.USER);
        }
        assertThat(userRepository.count()).isEqualTo(numberOfUsers);
    }

    private void callAndAssertSearchUsers(String keywords, Function<User, String>... fieldExtractors) {
        SearchHelperForTest<ServiceTriSearcherForTest<UsersListDto>, User, UsersListDto> searchHelper
                = createSearchHelper(userService::searchByNickname, keywords, fieldExtractors);
        initializeSearchHelper(searchHelper);
        callAndAssertSearch();
    }

    private SearchHelperForTest<ServiceTriSearcherForTest<UsersListDto>, User, UsersListDto>
        createSearchHelper(ServiceTriSearcherForTest<UsersListDto> searcher,
                           String keywords, Function<User, String>... fieldExtractors) {
        EntityConverterForTest<User, UsersListDto> entityConverter = new UserConverterForTest();
        return SearchHelperForTest.<ServiceTriSearcherForTest<UsersListDto>, User, UsersListDto>builder()
                .searcher(searcher).keywords(keywords)
                .totalContents(userRepository.findAll())
                .fieldExtractors(fieldExtractors)
                .page(1).limit(2)
                .entityConverter(entityConverter).build();
    }
}
