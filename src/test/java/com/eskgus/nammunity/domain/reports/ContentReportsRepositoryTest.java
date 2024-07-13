package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.config.TestConfig;
import com.eskgus.nammunity.converter.ContentReportsConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.common.Element;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.helper.PaginationTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.util.PaginationRepoUtil;
import com.eskgus.nammunity.util.PaginationTestUtil;
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
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.eskgus.nammunity.domain.enums.Fields.OTHER_REASONS;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    private User reporter;
    private User user;
    private Posts post;
    private Comments comment;

    private ContentReports latestPostReport;
    private ContentReports latestCommentReport;
    private ContentReports latestUserReport;

    private static final ContentType POSTS = ContentType.POSTS;
    private static final ContentType COMMENTS = ContentType.COMMENTS;
    private static final ContentType USERS = ContentType.USERS;
    private static final ContentReportsConverterForTest REPORTS_CONVERTER = new ContentReportsConverterForTest();
    private static final ReportsTestVisitor VISITOR = new ReportsTestVisitor(REPORTS_CONVERTER);

    @BeforeEach
    public void setUp() {
        Long user1Id = testDataHelper.signUp(1L, Role.USER);
        this.reporter = assertOptionalAndGetEntity(userRepository::findById, user1Id);

        Long user2Id = testDataHelper.signUp(2L, Role.USER);
        this.user = assertOptionalAndGetEntity(userRepository::findById, user2Id);

        Long postId = testDataHelper.savePosts(reporter);
        this.post = assertOptionalAndGetEntity(postsRepository::findById, postId);

        Long commentId = testDataHelper.saveComments(postId, reporter);
        this.comment = assertOptionalAndGetEntity(commentsRepository::findById, commentId);
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    public void findReporterByPosts() {
        testFindFieldByElement(POSTS, this::testFindReporterByElement);
    }

    @Test
    public void findReporterByComments() {
        testFindFieldByElement(COMMENTS, this::testFindReporterByElement);
    }

    @Test
    public void findReporterByUsers() {
        testFindFieldByElement(USERS, this::testFindReporterByElement);
    }

    @Test
    public void findReportedDateByPosts() {
        testFindFieldByElement(POSTS, this::testFindReportedDateByElement);
    }

    @Test
    public void findReportedDateByComments() {
        testFindFieldByElement(COMMENTS, this::testFindReportedDateByElement);
    }

    @Test
    public void findReportedDateByUsers() {
        testFindFieldByElement(USERS, this::testFindReportedDateByElement);
    }

    @Test
    public void findReasonByPosts() {
        testFindFieldByElement(POSTS, this::testFindReasonByElement);
    }

    @Test
    public void findReasonByComments() {
        testFindFieldByElement(COMMENTS, this::testFindReasonByElement);
    }

    @Test
    public void findReasonByUsers() {
        testFindFieldByElement(USERS, this::testFindReasonByElement);
    }

    @Test
    public void findOtherReasonByPosts() {
        testFindOtherReasonByElement(POSTS);
    }

    @Test
    public void findOtherReasonByComments() {
        testFindOtherReasonByElement(COMMENTS);
    }

    @Test
    public void findOtherReasonByUsers() {
        testFindOtherReasonByElement(USERS);
    }

    @Test
    public void findReportsByPosts() {
        testFindReportsByElement(POSTS);
    }

    @Test
    public void findReportsByComments() {
        testFindReportsByElement(COMMENTS);
    }

    @Test
    public void findReportsByUsers() {
        testFindReportsByElement(USERS);
    }

    @Test
    public void countReportsByPostTypeAndUser() {
        testCountReportsByContentTypeAndUser(POSTS, post.getUser());
    }

    @Test
    public void countReportsByCommentTypeAndUser() {
        testCountReportsByContentTypeAndUser(COMMENTS, comment.getUser());
    }

    @Test
    public void countReportsByUserTypeAndUser() {
        testCountReportsByContentTypeAndUser(USERS, user);
    }

    @Test
    public void countReportsByPosts() {
        testCountReportsByElement(POSTS);
    }

    @Test
    public void countReportsByComments() {
        testCountReportsByElement(COMMENTS);
    }

    @Test
    public void countReportsByUsers() {
        testCountReportsByElement(USERS);
    }

    private void testFindFieldByElement(ContentType contentType, BiConsumer<Element, ContentReports> tester) {
        // given
        Element element = getElement(contentType);

        ContentReports report = saveReports(contentType);

        // when/then
        tester.accept(element, report);
    }

    private void testFindReporterByElement(Element element, ContentReports report) {
        // when
        User result = contentReportsRepository.findReporterByElement(element);

        // then
        assertEquals(report.getReporter().getId(), result.getId());
    }

    private void testFindReportedDateByElement(Element element, ContentReports report) {
        // when
        LocalDateTime result = contentReportsRepository.findReportedDateByElement(element);

        // then
        assertEquals(report.getCreatedDate().withNano(0), result.withNano(0));
    }

    private void testFindReasonByElement(Element element, ContentReports report) {
        // when
        Reasons result = contentReportsRepository.findReasonByElement(element);

        // then
        assertEquals(report.getReasons().getId(), result.getId());
    }

    private void testFindOtherReasonByElement(ContentType contentType) {
        // given
        Element element = getElement(contentType);

        ContentReports report = saveReportsWithOtherReason(contentType);

        Reasons reason = report.getReasons();

        // when
        String result = contentReportsRepository.findOtherReasonByElement(element, reason);

        // then
        assertEquals(report.getOtherReasons(), result);
    }

    private void testFindReportsByElement(ContentType contentType) {
        // given
        Element element = getElement(contentType);

        saveReports(contentType);

        Pageable pageable = PaginationRepoUtil.createPageable(1, 3);

        Predicate<ContentReports> filter = createFilter(element);
        Page<ContentReportDetailListDto> reportsPage = createReportsPage(filter, pageable);

        // when
        Page<ContentReportDetailListDto> result = contentReportsRepository.findByElement(element, pageable);

        // then
        assertReportsPage(result, reportsPage);
    }

    private void testCountReportsByContentTypeAndUser(ContentType contentType, User user) {
        // given
        long numberOfReport = saveReportsAndGetNumberOfReports(contentType);

        // when
        long result = contentReportsRepository.countReportsByContentTypeAndUser(contentType, user);

        // then
        assertEquals(numberOfReport, result);
    }

    private void testCountReportsByElement(ContentType contentType) {
        // given
        Element element = getElement(contentType);

        long numberOfReport = saveReportsAndGetNumberOfReports(contentType);

        // when
        long result = contentReportsRepository.countByElement(element);

        // then
        assertEquals(numberOfReport, result);
    }

    private Element getElement(ContentType contentType) {
        return switch (contentType) {
            case POSTS -> post;
            case COMMENTS -> comment;
            case USERS -> user;
        };
    }

    private long saveReportsAndGetNumberOfReports(ContentType contentType) {
        ContentReports report = saveReports(contentType);

        return getNumberOfReports(contentType, report.getId());
    }

    private ContentReports saveReports(ContentType contentType) {
        this.latestPostReport = savePostReports();
        this.latestCommentReport = saveCommentReports();
        this.latestUserReport = saveUserReports();

        return getLatestContentReport(contentType);
    }

    private ContentReports saveReportsWithOtherReason(ContentType contentType) {
        for (int i = 0; i < 3; i++) {
            String otherReason = OTHER_REASONS.getKey() + i;

            this.latestPostReport = savePostReportWithOtherReason(otherReason);
            this.latestCommentReport = saveCommentReportWithOtherReason(otherReason);
            this.latestUserReport = saveUserReportWithOtherReason(otherReason);
        }

        return getLatestContentReport(contentType);
    }

    private ContentReports savePostReports() {
        Long latestPostReportId = testDataHelper.savePostReports(post.getId(), reporter);
        return assertOptionalAndGetContentReports(latestPostReportId);
    }

    private ContentReports saveCommentReports() {
        Long latestCommentReportId = testDataHelper.saveCommentReports(comment.getId(), reporter);
        return assertOptionalAndGetContentReports(latestCommentReportId);
    }

    private ContentReports saveUserReports() {
        Long latestUserReportId = testDataHelper.saveUserReports(user, reporter);
        return assertOptionalAndGetContentReports(latestUserReportId);
    }

    private ContentReports savePostReportWithOtherReason(String otherReason) {
        Long postReportId = testDataHelper.savePostReportsWithOtherReason(post.getId(), reporter, otherReason);
        return assertOptionalAndGetContentReports(postReportId);
    }

    private ContentReports saveCommentReportWithOtherReason(String otherReason) {
        Long commentReportId = testDataHelper.saveCommentReportsWithOtherReason(comment.getId(), reporter, otherReason);
        return assertOptionalAndGetContentReports(commentReportId);
    }

    private ContentReports saveUserReportWithOtherReason(String otherReason) {
        Long userReportId = testDataHelper.saveUserReportsWithOtherReason(user, reporter, otherReason);
        return assertOptionalAndGetContentReports(userReportId);
    }

    private ContentReports getLatestContentReport(ContentType contentType) {
        return switch (contentType) {
            case POSTS -> latestPostReport;
            case COMMENTS -> latestCommentReport;
            case USERS -> latestUserReport;
        };
    }

    private Predicate<ContentReports> createFilter(Element element) {
        element.accept(VISITOR);

        return VISITOR.getFilter();
    }

    private Page<ContentReportDetailListDto> createReportsPage(Predicate<ContentReports> filter,
                                                               Pageable pageable) {
        Stream<ContentReports> filteredReportsStream = contentReportsRepository.findAll().stream().filter(filter);

        return PaginationTestUtil.createPageWithContent(filteredReportsStream, REPORTS_CONVERTER, pageable);
    }

    private void assertReportsPage(Page<ContentReportDetailListDto> result,
                                   Page<ContentReportDetailListDto> reportsPage) {
        PaginationTestHelper<ContentReportDetailListDto, ContentReports> paginationHelper
                = new PaginationTestHelper<>(result, reportsPage, REPORTS_CONVERTER);
        paginationHelper.assertContents();
    }

    private long getNumberOfReports(ContentType contentType, long latestReportId) {
        long startingReportId = switch (contentType) {
            case POSTS -> 0;
            case COMMENTS -> latestPostReport.getId();
            case USERS -> latestCommentReport.getId();
        };

        return latestReportId - startingReportId;
    }

    private ContentReports assertOptionalAndGetContentReports(Long reportId) {
        return assertOptionalAndGetEntity(contentReportsRepository::findById, reportId);
    }

    private <Entity> Entity assertOptionalAndGetEntity(Function<Long, Optional<Entity>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }
}
