package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.config.TestConfig;
import com.eskgus.nammunity.converter.ContentReportsConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.helper.PaginationTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.reports.ContentReportDetailListDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.eskgus.nammunity.util.PaginationRepoUtil.createPageable;
import static com.eskgus.nammunity.util.PaginationTestUtil.createPageWithContent;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import({ TestDataHelper.class, TestConfig.class })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ContentReportsRepositoryTest {
    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private ContentReportsRepository contentReportsRepository;

    private User user1;
    private User user2;
    private Posts post;
    private Comments comment;

    private ContentReports latestPostReport;
    private ContentReports latestCommentReport;
    private ContentReports latestUserReport;

    private final ContentReportsConverterForTest entityConverter = new ContentReportsConverterForTest();

    @BeforeEach
    public void setUp() {
        Long user1Id = testDataHelper.signUp(1L, Role.USER);
        this.user1 = assertOptionalAndGetEntity(userRepository::findById, user1Id);

        Long user2Id = testDataHelper.signUp(2L, Role.USER);
        this.user2 = assertOptionalAndGetEntity(userRepository::findById, user2Id);

        Long post1Id = testDataHelper.savePosts(user1);
        this.post = assertOptionalAndGetEntity(postsRepository::findById, post1Id);

        Long comment1Id = testDataHelper.saveComments(post1Id, user1);
        this.comment = assertOptionalAndGetEntity(commentsRepository::findById, comment1Id);
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    public void findReporterByContents() {
        // 게시글 신고 + 댓글 신고 + 사용자 신고
        saveReports();

        callAndAssertFindReporterByContents(post, latestPostReport);
        callAndAssertFindReporterByContents(comment, latestCommentReport);
        callAndAssertFindReporterByContents(user1, latestUserReport);
    }

    private void saveReports() {
        Long postReportId = testDataHelper.savePostReports(post.getId(), user2);
        this.latestPostReport = assertOptionalAndGetEntity(contentReportsRepository::findById, postReportId);

        Long commentReportId = testDataHelper.saveCommentReports(comment.getId(), user2);
        this.latestCommentReport = assertOptionalAndGetEntity(contentReportsRepository::findById, commentReportId);

        Long userReportId = testDataHelper.saveUserReports(user1, user2);
        this.latestUserReport = assertOptionalAndGetEntity(contentReportsRepository::findById, userReportId);
    }

    private <T> void callAndAssertFindReporterByContents(T contents, ContentReports expectedReport) {
        User actualReporter = contentReportsRepository.findReporterByContents(contents);
        assertThat(actualReporter.getId()).isEqualTo(expectedReport.getReporter().getId());
    }

    @Test
    public void findReportedDateByContents() {
        saveReports();

        callAndAssertFindReportedDateByContents(post, latestPostReport);
        callAndAssertFindReportedDateByContents(comment, latestCommentReport);
        callAndAssertFindReportedDateByContents(user1, latestUserReport);
    }

    private <T> void callAndAssertFindReportedDateByContents(T contents, ContentReports expectedReport) {
        LocalDateTime actualReportedDate = contentReportsRepository.findReportedDateByContents(contents);
        assertThat(actualReportedDate.withNano(0)).isEqualTo(expectedReport.getCreatedDate().withNano(0));
    }

    @Test
    public void findReasonByContents() {
        saveReports();

        callAndAssertFindReasonByContents(post, latestPostReport);
        callAndAssertFindReasonByContents(comment, latestCommentReport);
        callAndAssertFindReasonByContents(user1, latestUserReport);
    }

    public <T> void callAndAssertFindReasonByContents(T contents, ContentReports expectedReport) {
        Reasons actualReason = contentReportsRepository.findReasonByContents(contents);
        assertThat(actualReason.getId()).isEqualTo(expectedReport.getReasons().getId());
    }

    @Test
    public void findOtherReasonByContents() {
        saveReportsWithOtherReason();

        callAndAssertFindOtherReasonByContents(post, latestPostReport);
        callAndAssertFindOtherReasonByContents(comment, latestCommentReport);
        callAndAssertFindOtherReasonByContents(user1, latestUserReport);
    }

    private void saveReportsWithOtherReason() {
        String[] otherReasons = { "기타 사유 1", "기타 사유 2", "기타 사유 3" };
        Long postReportId = 0L;
        Long commentReportId = 0L;
        Long userReportId = 0L;
        for (String otherReason : otherReasons) {
            postReportId = testDataHelper.savePostReportsWithOtherReason(post.getId(), user2, otherReason);
            commentReportId = testDataHelper.saveCommentReportsWithOtherReason(comment.getId(), user2, otherReason);
            userReportId = testDataHelper.saveUserReportsWithOtherReason(user1, user2, otherReason);
        }

        this.latestPostReport = assertOptionalAndGetEntity(contentReportsRepository::findById, postReportId);
        this.latestCommentReport = assertOptionalAndGetEntity(contentReportsRepository::findById, commentReportId);
        this.latestUserReport = assertOptionalAndGetEntity(contentReportsRepository::findById, userReportId);
    }

    private <T> void callAndAssertFindOtherReasonByContents(T contents, ContentReports expectedReport) {
        Reasons reason = expectedReport.getReasons();
        String actualOtherReason = contentReportsRepository.findOtherReasonByContents(contents, reason);
        assertThat(actualOtherReason).isEqualTo(expectedReport.getOtherReasons());
    }

    @Test
    public void findByContents() {
        saveReports();

        callAndAssertFindByContents(post);
        callAndAssertFindByContents(comment);
        callAndAssertFindByContents(user1);
    }

    private <T> void callAndAssertFindByContents(T content) {
        int page = 1;
        int size = 3;

        Pageable pageable = createPageable(page, size);

        Page<ContentReportDetailListDto> actualContents = contentReportsRepository.findByContents(content, pageable);
        Page<ContentReportDetailListDto> expectedContents = createExpectedContents(content, pageable);

        assertContents(actualContents, expectedContents);
    }

    private <T> Page<ContentReportDetailListDto> createExpectedContents(T content, Pageable pageable) {
        Predicate<ContentReports> filter = createFilterByContent(content);

        Stream<ContentReports> filteredReportsStream = contentReportsRepository.findAll().stream()
                .filter(filter);
        return createPageWithContent(filteredReportsStream, entityConverter, pageable);
    }

    private <T> Predicate<ContentReports> createFilterByContent(T content) {
        if (content instanceof Posts) {
            return report -> entityConverter.extractPostId(report).equals(((Posts) content).getId());
        } else if (content instanceof Comments) {
            return report -> entityConverter.extractCommentId(report).equals(((Comments) content).getId());
        } else {
            return report -> entityConverter.extractUserId(report).equals(((User) content).getId());
        }
    }

    private void assertContents(Page<ContentReportDetailListDto> actualContents,
                                Page<ContentReportDetailListDto> expectedContents) {
        PaginationTestHelper<ContentReportDetailListDto, ContentReports> paginationHelper
                = new PaginationTestHelper<>(actualContents, expectedContents, entityConverter);
        paginationHelper.assertContents();
    }

    @Test
    public void countReportsByContentTypeAndUser() {
        ContentType postType = ContentType.POSTS;
        ContentType commentType = ContentType.COMMENTS;
        ContentType userType = ContentType.USERS;

        // 1. 게시글
        // 1-1. 게시글 신고 x 후 호출
        callAndAssertCountReportsByContentTypeAndUser(postType, 0);
        // 1-2. 게시글 신고 후 호출
        Long postReportId = saveReportsAndGetLatestReportId(testDataHelper::savePostReports, post.getId(), user2);
        callAndAssertCountReportsByContentTypeAndUser(postType, postReportId);

        // 2. 댓글
        // 2-1. 댓글 신고 x 후 호출
        callAndAssertCountReportsByContentTypeAndUser(commentType, 0);
        // 2-2. 댓글 신고 후 호출
        Long commentReportId = saveReportsAndGetLatestReportId(testDataHelper::saveCommentReports, comment.getId(), user2);
        callAndAssertCountReportsByContentTypeAndUser(commentType, commentReportId - postReportId);

        // 3. 사용자
        // 3-1. 사용자 신고 x 후 호출
        callAndAssertCountReportsByContentTypeAndUser(userType, 0);
        // 3-2. 사용자 신고 후 호출
        Long userReportId = saveReportsAndGetLatestReportId(testDataHelper::saveUserReports, user1, user2);
        callAndAssertCountReportsByContentTypeAndUser(userType, userReportId - commentReportId);
    }

    private void callAndAssertCountReportsByContentTypeAndUser(ContentType contentType, long expectedCount) {
        long actualCount = contentReportsRepository.countReportsByContentTypeAndUser(contentType, user1);
        assertThat(actualCount).isEqualTo(expectedCount);
    }

    private <T> Long saveReportsAndGetLatestReportId(BiFunction<T, User, Long> saver, T t, User user) {
        Long latestReportId = saver.apply(t, user);
        assertOptionalAndGetEntity(contentReportsRepository::findById, latestReportId);
        return latestReportId;
    }

    @Test
    public void countByContents() {
        // 1. 신고 x 후 호출
        callAndAssertCountByContents(post, 0L);
        callAndAssertCountByContents(comment, 0L);
        callAndAssertCountByContents(user1, 0L);

        // 2. 신고 후 호출
        saveReports();

        callAndAssertCountByContents(post, latestPostReport.getId());
        callAndAssertCountByContents(comment,
                latestCommentReport.getId() - latestPostReport.getId());
        callAndAssertCountByContents(user1,
                latestUserReport.getId() - latestCommentReport.getId());
    }

    private <T> void callAndAssertCountByContents(T contents, long expectedCountByContents) {
        long actualCountByContents = contentReportsRepository.countByContents(contents);
        assertThat(actualCountByContents).isEqualTo(expectedCountByContents);
    }
}
