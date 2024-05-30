package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.converter.CommentsConverterForTest;
import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.converter.PostsConverterForTest;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.reports.ContentReportSummaryRepository;
import com.eskgus.nammunity.domain.reports.ContentReportsRepository;
import com.eskgus.nammunity.domain.user.BannedUsersRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.helper.ContentsPageDtoTestHelper;
import com.eskgus.nammunity.helper.ContentsPageMoreDtoTestHelper;
import com.eskgus.nammunity.service.comments.CommentsSearchService;
import com.eskgus.nammunity.service.likes.LikesSearchService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageMoreDtos;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.user.ActivityHistoryDto;
import com.eskgus.nammunity.web.dto.user.BannedHistoryDto;
import com.eskgus.nammunity.web.dto.user.CommentsHistoryDto;
import com.eskgus.nammunity.web.dto.user.PostsHistoryDto;
import org.assertj.core.util.TriFunction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.Principal;
import java.time.Period;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ActivityServiceTest {
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
    private ContentReportSummaryRepository reportSummaryRepository;

    @Autowired
    private BannedUsersRepository bannedUsersRepository;

    @Autowired
    private LikesRepository likesRepository;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private PostsService postsService;

    @Autowired
    private CommentsSearchService commentsSearchService;

    @Autowired
    private LikesSearchService likesSearchService;

    private User user1;
    private User user2;

    private final int page = 1;
    private ActivityHistoryDto activityHistoryDto;
    private final ContentType postType = ContentType.POSTS;
    private final ContentType commentType = ContentType.COMMENTS;
    private final ContentType userType = ContentType.USERS;

    @BeforeEach
    public void setUp() {
        Long user1Id = testDB.signUp(1L, Role.USER);
        this.user1 = assertOptionalAndGetEntity(userRepository::findById, user1Id);

        Long user2Id = testDB.signUp(2L, Role.ADMIN);
        this.user2 = assertOptionalAndGetEntity(userRepository::findById, user2Id);
    }

    private <T, U> T assertOptionalAndGetEntity(Function<U, Optional<T>> finder, U content) {
        return testDB.assertOptionalAndGetEntity(finder, content);
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

        // 1. type = "posts"
        callAndAssertFindActivityHistory(postType.getDetailInEng());

        // 2. type = "comments"
        callAndAssertFindActivityHistory(commentType.getDetailInEng());
    }

    private void savePostsAndComments() {
        int numberOfContents = 11;
        for (int i = 0; i < numberOfContents; i++) {
            Long postId = testDB.savePosts(user1);
            testDB.saveComments(postId, user1);
        }
        assertThat(postsRepository.count()).isEqualTo(numberOfContents);
        assertThat(commentsRepository.count()).isEqualTo(numberOfContents);
    }

    private void reportUser() {
        Long latestUserReportId = testDB.saveUserReports(user1, user2);
        assertThat(contentReportsRepository.count()).isEqualTo(latestUserReportId);
    }

    private void saveUserReportSummary() {
        Long reportSummaryId = testDB.saveUserReportSummary(user1, user2);
        assertThat(reportSummaryRepository.count()).isEqualTo(reportSummaryId);
    }

    private void banUser() {
        Long bannedUserId = testDB.saveBannedUsers(user1, Period.ofWeeks(1));
        assertThat(bannedUsersRepository.count()).isEqualTo(bannedUserId);
    }

    private void callAndAssertFindActivityHistory(String type) {
        this.activityHistoryDto = activityService.findActivityHistory(user1.getId(), type, page);
        assertActivityHistoryDto(type);
    }

    private void assertActivityHistoryDto(String type) {
        assertThat(activityHistoryDto.getUsersListDto().getId()).isEqualTo(user1.getId());
        assertUserBan(activityHistoryDto.getBannedHistoryDto());
        assertContentsHistoryDto(type);
        assertNumberOfReports();
    }

    private void assertContentsHistoryDto(String type) {
        long actualNumberOfPosts;
        long actualNumberOfComments;
        if (type.equals(ContentType.POSTS.getDetailInEng())) {
            PostsHistoryDto postsHistoryDto = activityHistoryDto.getPostsHistoryDto();

            assertContentsPage(postsService::findByUser, postsHistoryDto.getContentsPage(),
                    new PostsConverterForTest());

            actualNumberOfPosts = postsHistoryDto.getNumberOfPosts();
            actualNumberOfComments = postsHistoryDto.getNumberOfComments();
        } else {
            CommentsHistoryDto commentsHistoryDto = activityHistoryDto.getCommentsHistoryDto();

            assertContentsPage(commentsSearchService::findByUser, commentsHistoryDto.getContentsPage(),
                    new CommentsConverterForTest<>(CommentsListDto.class));

            actualNumberOfPosts = commentsHistoryDto.getNumberOfPosts();
            actualNumberOfComments = commentsHistoryDto.getNumberOfComments();
        }

        assertNumberOfContent(actualNumberOfPosts, postsRepository::countByUser);
        assertNumberOfContent(actualNumberOfComments, commentsRepository::countByUser);
    }

    private <T, U> void assertContentsPage(TriFunction<User, Integer, Integer, Page<T>> finder,
                                           ContentsPageDto<T> actualResult,
                                           EntityConverterForTest<T, U> entityConverter) {
        int size = 10;
        Page<T> expectedContents = finder.apply(user1, page, size);
        ContentsPageDtoTestHelper<T, U> findHelper
                = ContentsPageDtoTestHelper.<T, U>builder()
                .actualResult(actualResult).expectedContents(expectedContents)
                .entityConverter(entityConverter).build();
        findHelper.createExpectedResultAndAssertContentsPage();
    }

    private void assertNumberOfContent(long actualNumberOfContent, Function<User, Long> expectedCounter) {
        assertThat(actualNumberOfContent).isEqualTo(expectedCounter.apply(user1));
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
        long expectedNumberOfReport = contentReportsRepository.countReportsByContentTypeAndUser(contentType, user1);
        assertThat(actualNumberOfReport).isEqualTo(expectedNumberOfReport);
    }

    private void assertUserBan(BannedHistoryDto bannedHistoryDto) {
        int expectedCount = assertOptionalAndGetEntity(bannedUsersRepository::findByUser, user1).getCount();
        assertThat(bannedHistoryDto.getCount()).isEqualTo(expectedCount);
    }

    @Test
    public void getMyPage() {
        savePostsAndComments();
        saveLikes();

        callAndAssertGetMyPage();
    }

    private void saveLikes() {
        long numberOfPosts = postsRepository.count();
        for (long i = 1; i <= numberOfPosts; i++) {
            testDB.savePostLikes(i, user1);
        }

        long numberOfComments = commentsRepository.count();
        for (long i = 1; i <= numberOfComments; i++) {
            testDB.saveCommentLikes(i, user1);
        }

        assertThat(likesRepository.count()).isEqualTo(numberOfPosts + numberOfComments);
    }

    private void callAndAssertGetMyPage() {
        int size = 5;

        ContentsPageMoreDtos<PostsListDto, CommentsListDto, LikesListDto> actualResult = callGetMyPageAndGetActualResult();

        Page<PostsListDto> postsPage = postsService.findByUser(user1, page, size);
        Page<CommentsListDto> commentsPage = commentsSearchService.findByUser(user1, page, size);
        Page<LikesListDto> likesPage = likesSearchService.findLikesByUser(user1, likesRepository::findByUser, page, size);

        ContentsPageMoreDtoTestHelper<PostsListDto, CommentsListDto, LikesListDto> findHelper
                = new ContentsPageMoreDtoTestHelper<>(actualResult, postsPage, commentsPage, likesPage);
        findHelper.createExpectedResultAndAssertContentsPageMore();
    }

    private ContentsPageMoreDtos<PostsListDto, CommentsListDto, LikesListDto> callGetMyPageAndGetActualResult() {
        Principal principal = createPrincipalWithUser(user1);
        return activityService.getMyPage(principal);
    }

    private Principal createPrincipalWithUser(User user) {
        return user::getUsername;
    }
}
