package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.config.TestConfig;
import com.eskgus.nammunity.converter.ContentReportSummaryConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.helper.PaginationTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
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

import static com.eskgus.nammunity.domain.enums.ContentType.*;
import static com.eskgus.nammunity.util.PaginationRepoUtil.createPageable;
import static com.eskgus.nammunity.util.PaginationTestUtil.createPageWithContent;
import static org.assertj.core.api.Assertions.assertThat;

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

    @Autowired
    private TypesRepository typesRepository;

    private User user1;
    private User user2;
    private Posts post;
    private Comments comment;
    private final int page = 1;
    private final ContentReportSummaryConverterForTest entityConverter = new ContentReportSummaryConverterForTest();

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

    private <T, U> T assertOptionalAndGetEntity(Function<U, Optional<T>> finder, U content) {
        return testDataHelper.assertOptionalAndGetEntity(finder, content);
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    public void existsByContents() {
        // 1. 신고 요약 저장 x 후 호출
        callAndAssertExistsByContents(post, false);
        callAndAssertExistsByContents(comment, false);
        callAndAssertExistsByContents(user1, false);

        // 2. 신고 요약 저장 후 호출
        saveReportSummaries();

        callAndAssertExistsByContents(post, true);
        callAndAssertExistsByContents(comment, true);
        callAndAssertExistsByContents(user1, true);
    }

    private <T> void callAndAssertExistsByContents(T contents, boolean expectedResult) {
        boolean actualResult = contentReportSummaryRepository.existsByContents(contents);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    private void saveReportSummaries() {
        Long postReportSummaryId = testDataHelper.savePostReportSummary(post, user1);
        assertOptionalAndGetEntity(contentReportSummaryRepository::findById, postReportSummaryId);

        Long commentReportSummaryId = testDataHelper.saveCommentReportSummary(comment, user1);
        assertOptionalAndGetEntity(contentReportSummaryRepository::findById, commentReportSummaryId);

        Long userReportSummaryId = testDataHelper.saveUserReportSummary(user1, user2);
        assertOptionalAndGetEntity(contentReportSummaryRepository::findById, userReportSummaryId);
    }

    @Test
    public void findByContents() {
        saveReportSummaries();

        callAndAssertFindByContents(post);
        callAndAssertFindByContents(comment);
        callAndAssertFindByContents(user1);
    }

    private <T> void callAndAssertFindByContents(T contents) {
        ContentReportSummary reportSummary = contentReportSummaryRepository.findByContents(contents);
        assertFindByContents(reportSummary, contents);
    }

    private <T> void assertFindByContents(ContentReportSummary reportSummary, T contents) {
        assertThat(reportSummary).isNotNull();
        assertContentId(reportSummary, contents);
    }

    private <T> void assertContentId(ContentReportSummary reportSummary, T contents) {
        Long actualId = getActualId(reportSummary);
        Long expectedId = getExpectedId(contents);
        assertThat(actualId).isEqualTo(expectedId);
    }

    private Long getActualId(ContentReportSummary reportSummary) {
        String type = reportSummary.getTypes().getDetail();
        if (POSTS.getDetail().equals(type)) {
            return reportSummary.getPosts().getId();
        } else if (COMMENTS.getDetail().equals(type)) {
            return reportSummary.getComments().getId();
        } else {
            return reportSummary.getUser().getId();
        }
    }

    private <T> Long getExpectedId(T contents) {
        if (contents instanceof Posts) {
            return ((Posts) contents).getId();
        } else if (contents instanceof Comments) {
            return ((Comments) contents).getId();
        } else {
            return ((User) contents).getId();
        }
    }

    @Test
    public void findAllDesc() {
        saveReportSummaries();

        callAndAssertFindAllDesc();
    }

    private void callAndAssertFindAllDesc() {
        int size = 2;

        Pageable pageable = createPageable(page, size);

        Page<ContentReportSummaryDto> actualContents = contentReportSummaryRepository.findAllDesc(pageable);
        Page<ContentReportSummaryDto> expectedContents = createExpectedContents(null, pageable);

        assertContents(actualContents, expectedContents);
    }

    private Page<ContentReportSummaryDto> createExpectedContents(Predicate<ContentReportSummary> filter,
                                                                 Pageable pageable) {
        Stream<ContentReportSummary> filteredReportSummaryStream = filter != null
                ? contentReportSummaryRepository.findAll().stream().filter(filter)
                    : contentReportSummaryRepository.findAll().stream();
        return createPageWithContent(filteredReportSummaryStream, entityConverter, pageable);
    }

    private void assertContents(Page<ContentReportSummaryDto> actualContents,
                                Page<ContentReportSummaryDto> expectedContents) {
        PaginationTestHelper<ContentReportSummaryDto, ContentReportSummary> paginationHelper
                = new PaginationTestHelper<>(actualContents, expectedContents, entityConverter);
        paginationHelper.assertContents();
    }

    @Test
    public void findByTypes() {
        saveReportSummaries();

        Types postType = assertOptionalAndGetEntity(typesRepository::findByDetail, POSTS.getDetail());
        callAndAssertFindByTypes(postType);

        Types commentType = assertOptionalAndGetEntity(typesRepository::findByDetail, COMMENTS.getDetail());
        callAndAssertFindByTypes(commentType);

        Types userType = assertOptionalAndGetEntity(typesRepository::findByDetail, USERS.getDetail());
        callAndAssertFindByTypes(userType);
    }

    private void callAndAssertFindByTypes(Types type) {
        int size = 2;

        Pageable pageable = createPageable(page, size);

        Page<ContentReportSummaryDto> actualContents = contentReportSummaryRepository.findByTypes(type, pageable);
        Page<ContentReportSummaryDto> expectedContents = createExpectedContentsByTypes(type, pageable);

        assertContents(actualContents, expectedContents);
    }

    private Page<ContentReportSummaryDto> createExpectedContentsByTypes(Types type, Pageable pageable) {
        Predicate<ContentReportSummary> filter =
                reportSummary -> entityConverter.extractTypeId(reportSummary).equals(type.getId());
        return createExpectedContents(filter, pageable);
    }

    @Test
    public void findByUser() {
        callAndAssertFindByUser(false);

        saveReportSummaries();
        callAndAssertFindByUser(true);
    }

    private void callAndAssertFindByUser(boolean expectedResult) {
        Optional<ContentReportSummary> result = contentReportSummaryRepository.findByUser(user1);
        assertThat(result.isPresent()).isEqualTo(expectedResult);
    }

    @Test
    public void deleteByContents() {
        saveReportSummaries();

        callAndAssertDeleteByContents(post);
        callAndAssertDeleteByContents(comment);
        callAndAssertDeleteByContents(user1);
    }

    private <T> void callAndAssertDeleteByContents(T contents) {
        contentReportSummaryRepository.deleteByContents(contents);
        assertDeleteByContents(contents);
    }

    private <T> void assertDeleteByContents(T contents) {
        ContentReportSummary reportSummary = contentReportSummaryRepository.findByContents(contents);
        assertThat(reportSummary).isNull();
    }
}
