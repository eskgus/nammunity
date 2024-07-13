package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.config.TestConfig;
import com.eskgus.nammunity.converter.ContentReportSummaryConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.common.Element;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.helper.PaginationTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.util.PaginationRepoUtil;
import com.eskgus.nammunity.util.PaginationTestUtil;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDto;
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

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import({ TestDataHelper.class, TestConfig.class })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ContentReportSummaryRepositoryTest {
    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private ContentReportSummaryRepository contentReportSummaryRepository;

    private User reporter;
    private User user;
    private Posts post;
    private Comments comment;

    private static final ContentType POSTS = ContentType.POSTS;
    private static final ContentType COMMENTS = ContentType.COMMENTS;
    private static final ContentType USERS = ContentType.USERS;
    private static final int PAGE = 1;
    private static final ContentReportSummaryConverterForTest REPORT_SUMMARY_CONVERTER = new ContentReportSummaryConverterForTest();

    @BeforeEach
    public void setUp() {
        Long reporterId = testDataHelper.signUp(1L, Role.USER);
        this.reporter = assertOptionalAndGetEntity(userRepository::findById, reporterId);

        Long userId = testDataHelper.signUp(2L, Role.USER);
        this.user = assertOptionalAndGetEntity(userRepository::findById, userId);

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
    public void findSummariesByUserWithoutReportSummaries() {
        // given
        // when/then
        testFindSummariesByUser(false);
    }

    @Test
    public void findSummariesByUserWithReportSummaries() {
        // given
        saveUserReportSummary();

        // when/then
        testFindSummariesByUser(true);
    }

    @Test
    public void existsSummariesByPosts() {
        testExistsSummariesByElement(post);
    }

    @Test
    public void existsSummariesByComments() {
        testExistsSummariesByElement(comment);
    }

    @Test
    public void existsSummariesByUsers() {
        testExistsSummariesByElement(user);
    }

    @Test
    public void findSummariesByPosts() {
        testFindSummariesByElement(POSTS);
    }

    @Test
    public void findSummariesByComments() {
        testFindSummariesByElement(COMMENTS);
    }

    @Test
    public void findSummariesByUsers() {
        testFindSummariesByElement(USERS);
    }

    @Test
    public void findAllSummariesDesc() {
        // given
        saveReportSummaries();

        Pageable pageable = createPageable();

        Page<ContentReportSummaryDto> summariesPage = createSummariesPageWithFilter(null, pageable);

        // when
        Page<ContentReportSummaryDto> result = contentReportSummaryRepository.findAllDesc(pageable);

        // then
        assertSummariesPage(result, summariesPage);
    }

    @Test
    public void findSummariesByPostType() {
        testFindSummariesByTypes(POSTS);
    }

    @Test
    public void findSummariesByCommentType() {
        testFindSummariesByTypes(COMMENTS);
    }

    @Test
    public void findSummariesByUserType() {
        testFindSummariesByTypes(USERS);
    }

    @Test
    public void deleteSummariesByPosts() {
        testDeleteSummariesByElement(POSTS);
    }

    @Test
    public void deleteSummariesByComments() {
        testDeleteSummariesByElement(COMMENTS);
    }

    @Test
    public void deleteSummariesByUsers() {
        testDeleteSummariesByElement(USERS);
    }

    private void testFindSummariesByUser(boolean present) {
        // when
        Optional<ContentReportSummary> result = contentReportSummaryRepository.findByUser(user);

        // then
        assertEquals(present, result.isPresent());
    }

    private void testExistsSummariesByElement(Element element) {
        // given
        saveReportSummaries();

        // when
        boolean result = contentReportSummaryRepository.existsByElement(element);

        // then
        assertTrue(result);
    }

    private void testFindSummariesByElement(ContentType contentType) {
        // given
        Element element = getElement(contentType);

        ContentReportSummary reportSummary = saveReportSummaries(contentType);

        // when
        ContentReportSummary result = contentReportSummaryRepository.findByElement(element);

        // then
        assertEquals(reportSummary, result);
    }

    private void testFindSummariesByTypes(ContentType contentType) {
        // given
        ContentReportSummary reportSummary = saveReportSummaries(contentType);

        Types type = reportSummary.getTypes();

        Pageable pageable = createPageable();

        Predicate<ContentReportSummary> filter = createFilter(type);
        Page<ContentReportSummaryDto> summariesPage = createSummariesPageWithFilter(filter, pageable);

        // when
        Page<ContentReportSummaryDto> result = contentReportSummaryRepository.findByTypes(type, pageable);

        // then
        assertSummariesPage(result, summariesPage);
    }

    private void testDeleteSummariesByElement(ContentType contentType) {
        // given
        Element element = getElement(contentType);

        ContentReportSummary reportSummary = saveReportSummaries(contentType);

        // when
        contentReportSummaryRepository.deleteByElement(element);

        // then
        boolean result = contentReportSummaryRepository.existsById(reportSummary.getId());
        assertFalse(result);
    }

    private Element getElement(ContentType contentType) {
        return switch (contentType) {
            case POSTS -> post;
            case COMMENTS -> comment;
            case USERS -> user;
        };
    }

    private void saveReportSummaries() {
        savePostReportSummary();
        saveCommentReportSummary();
        saveUserReportSummary();
    }

    private ContentReportSummary saveReportSummaries(ContentType contentType) {
        ContentReportSummary latestPostReportSummary = savePostReportSummary();
        ContentReportSummary latestCommentReportSummary = saveCommentReportSummary();
        ContentReportSummary latestUserReportSummary = saveUserReportSummary();

        return getLatestReportSummary(
                contentType, latestPostReportSummary, latestCommentReportSummary, latestUserReportSummary);
    }

    private ContentReportSummary savePostReportSummary() {
        Long postReportSummaryId = testDataHelper.savePostReportSummary(post, reporter);
        return assertOptionalAndGetReportSummary(postReportSummaryId);
    }

    private ContentReportSummary saveCommentReportSummary() {
        Long commentReportSummaryId = testDataHelper.saveCommentReportSummary(comment, reporter);
        return assertOptionalAndGetReportSummary(commentReportSummaryId);
    }

    private ContentReportSummary saveUserReportSummary() {
        Long userReportSummaryId = testDataHelper.saveUserReportSummary(user, reporter);
        return assertOptionalAndGetReportSummary(userReportSummaryId);
    }

    private ContentReportSummary getLatestReportSummary(ContentType contentType,
                                                        ContentReportSummary latestPostReportSummary,
                                                        ContentReportSummary latestCommentReportSummary,
                                                        ContentReportSummary latestUserReportSummary) {
        return switch (contentType) {
            case POSTS -> latestPostReportSummary;
            case COMMENTS -> latestCommentReportSummary;
            case USERS -> latestUserReportSummary;
        };
    }

    private Pageable createPageable() {
        return PaginationRepoUtil.createPageable(PAGE, 3);
    }

    private Predicate<ContentReportSummary> createFilter(Types type) {
        return reportSummary -> REPORT_SUMMARY_CONVERTER.extractTypeId(reportSummary).equals(type.getId());
    }

    private Page<ContentReportSummaryDto> createSummariesPageWithFilter(Predicate<ContentReportSummary> filter,
                                                                        Pageable pageable) {
        Stream<ContentReportSummary> summariesStream = contentReportSummaryRepository.findAll().stream();
        Stream<ContentReportSummary> filteredSummariesStream = filter == null
                ? summariesStream : summariesStream.filter(filter);

        return createPageWithContent(filteredSummariesStream, pageable);
    }

    private Page<ContentReportSummaryDto> createPageWithContent(Stream<ContentReportSummary> filteredSummariesStream,
                                                                Pageable pageable) {
        return PaginationTestUtil.createPageWithContent(filteredSummariesStream, REPORT_SUMMARY_CONVERTER, pageable);
    }

    private void assertSummariesPage(Page<ContentReportSummaryDto> result, Page<ContentReportSummaryDto> summariesPage) {
        PaginationTestHelper<ContentReportSummaryDto, ContentReportSummary> paginationHelper
                = new PaginationTestHelper<>(result, summariesPage, REPORT_SUMMARY_CONVERTER);
        paginationHelper.assertContents();
    }

    private ContentReportSummary assertOptionalAndGetReportSummary(Long reportSummaryId) {
        return assertOptionalAndGetEntity(contentReportSummaryRepository::findById, reportSummaryId);
    }

    private <Entity> Entity assertOptionalAndGetEntity(Function<Long, Optional<Entity>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }
}
