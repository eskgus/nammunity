package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.converter.*;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.helper.ContentsPageDtoTestHelper;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.reports.*;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.reports.*;
import com.eskgus.nammunity.web.dto.user.UsersListDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.Principal;
import java.util.*;
import java.util.function.Function;

import static com.eskgus.nammunity.util.PaginationRepoUtil.createPageable;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ReportsServiceTest {
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
    private ReasonsRepository reasonsRepository;

    @Autowired
    private ContentReportSummaryRepository contentReportSummaryRepository;

    @Autowired
    private TypesRepository typesRepository;

    @Autowired
    private ReportsService reportsService;

    private User user1;
    private User user2;
    private Posts post;
    private Comments comment;

    private Long latestCommentReportId;
    private Long latestUserReportId;

    private final int page = 1;

    @BeforeEach
    public void setUp() {
        Long user1Id = testDB.signUp(1L, Role.USER);
        this.user1 = assertOptionalAndGetEntity(userRepository::findById, user1Id);

        Long user2Id = testDB.signUp(2L, Role.ADMIN);
        this.user2 = assertOptionalAndGetEntity(userRepository::findById, user2Id);

        Long postId = testDB.savePosts(user1);
        this.post = assertOptionalAndGetEntity(postsRepository::findById, postId);

        Long commentId = testDB.saveComments(postId, user1);
        this.comment = assertOptionalAndGetEntity(commentsRepository::findById, commentId);
    }

    private <T, U> T assertOptionalAndGetEntity(Function<U, Optional<T>> finder, U content) {
        return testDB.assertOptionalAndGetEntity(finder, content);
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void saveContentReports() {
        Long postId = post.getId();
        Long commentId = comment.getId();
        Long user1Id = user1.getId();

        ContentType postType = ContentType.POSTS;
        ContentType commentType = ContentType.COMMENTS;
        ContentType userType = ContentType.USERS;

        // 1. 최초 신고 (신고 요약 저장 x)
        callAndAssertSaveContentReports(postId, postType, user2);
        callAndAssertSaveContentReports(commentId, commentType, user2);
        callAndAssertSaveContentReports(user1Id, userType, user2);
        assertThat(contentReportSummaryRepository.count()).isZero();

        // 2. 누적 신고 10 or 3개 이상 (신고 요약 저장 o)
        saveReports();

        callAndAssertSaveContentReports(postId, postType, user2);
        callAndAssertSaveContentReports(commentId, commentType, user2);
        callAndAssertSaveContentReports(user1Id, userType, user2);
        assertThat(contentReportSummaryRepository.count()).isEqualTo(latestUserReportId - latestCommentReportId);
    }

    private void callAndAssertSaveContentReports(Long contentId, ContentType contentType, User reporter) {
        Long reportId = callSaveContentReports(contentId, contentType, reporter);
        assertSaveContentReports(contentId, reporter, reportId);
    }

    private Long callSaveContentReports(Long contentId, ContentType contentType, User reporter) {
        ContentReportsSaveDto requestDto = createReportsSaveDto(contentId, contentType);
        Principal principal = reporter::getUsername;
        return reportsService.saveContentReports(requestDto, principal);
    }

    private ContentReportsSaveDto createReportsSaveDto(Long contentId, ContentType contentType) {
        Long reasonId = reasonsRepository.count();
        String otherReason = "기타 사유";

        ContentReportsSaveDto saveDto = new ContentReportsSaveDto();
        saveDto.setReasonsId(reasonId);
        saveDto.setOtherReasons(otherReason);
        switch (contentType) {
            case POSTS -> saveDto.setPostsId(contentId);
            case COMMENTS -> saveDto.setCommentsId(contentId);
            case USERS -> saveDto.setUserId(contentId);
        }
        return saveDto;
    }

    private void assertSaveContentReports(Long contentId, User reporter, Long reportId) {
        Optional<ContentReports> result = contentReportsRepository.findById(reportId);
        assertThat(result).isPresent();

        ContentReports report = result.get();
        assertThat(report.getReporter().getId()).isEqualTo(reporter.getId());
        assertContentId(report, contentId);
    }

    private void assertContentId(ContentReports report, Long expectedContentId) {
        Long actualContentId = getContentIdFromReport(report);
        assertThat(actualContentId).isEqualTo(expectedContentId);
    }

    private Long getContentIdFromReport(ContentReports report) {
        String type = report.getTypes().getDetail();
        if (type.equals(ContentType.POSTS.getDetailInKor())) {
            return report.getPosts().getId();
        } else if (type.equals(ContentType.COMMENTS.getDetailInKor())) {
            return report.getComments().getId();
        }
        return report.getUser().getId();
    }

    private void saveReports() {
        testDB.savePostReports(post.getId(), user2);
        this.latestCommentReportId = testDB.saveCommentReports(comment.getId(), user2);
        this.latestUserReportId = testDB.saveUserReports(user1, user2);
        assertThat(contentReportsRepository.count()).isEqualTo(latestUserReportId);
    }

    @Test
    public void listContentReportDetails() {
        saveReports();

        // 1. postId
        callAndAssertListContentReportDetails(post, new PostsListDto(post), ContentType.POSTS);

        // 2. commentId
        callAndAssertListContentReportDetails(comment, new CommentsListDto(comment), ContentType.COMMENTS);

        // 3. userId
        callAndAssertListContentReportDetails(user1, new UsersListDto(user1), ContentType.USERS);

    }

    private <U, T> void callAndAssertListContentReportDetails(U content, T expectedContentListDto,
                                                              ContentType contentType) {
        Long contentId = getIdFromContent(content);
        ContentReportDetailRequestDto requestDto = createRequestDto(contentId, contentType);
        ContentReportDetailDto<T> actualResult = reportsService.listContentReportDetails(requestDto);
        ContentReportDetailDto<T> expectedResult = createExpectedResult(expectedContentListDto, contentType);
        Page<ContentReportDetailListDto> expectedContents = createExpectedContents(content);

        assertContentReportDetailDto(actualResult, expectedResult, expectedContents);
    }

    private <T> Long getIdFromContent(T contents) {
        if (contents instanceof Posts) {
            return ((Posts) contents).getId();
        } else if (contents instanceof Comments) {
            return ((Comments) contents).getId();
        }
        return ((User) contents).getId();
    }

    private ContentReportDetailRequestDto createRequestDto(Long contentId, ContentType contentType) {
        if (contentType.equals(ContentType.POSTS)) {
            return ContentReportDetailRequestDto.builder().postId(contentId).page(page).build();
        } else if (contentType.equals(ContentType.COMMENTS)) {
            return ContentReportDetailRequestDto.builder().commentId(contentId).page(page).build();
        } else {
            return ContentReportDetailRequestDto.builder().userId(contentId).page(page).build();
        }
    }

    private <T> ContentReportDetailDto<T> createExpectedResult(T expectedContentListDto,
                                                               ContentType contentType) {
        Types expectedType = assertOptionalAndGetEntity(typesRepository::findByDetail, contentType.getDetailInKor());
        return ContentReportDetailDto.<T>builder()
                .type(expectedType).contentListDto(expectedContentListDto).build();
    }

    private <U> Page<ContentReportDetailListDto> createExpectedContents(U expectedContent) {
        Pageable pageable = createPageable(page, 10);
        return contentReportsRepository.findByContents(expectedContent, pageable);
    }

    private <T> void assertContentReportDetailDto(ContentReportDetailDto<T> actualResult,
                                              ContentReportDetailDto<T> expectedResult,
                                              Page<ContentReportDetailListDto> expectedContents) {
        assertThat(actualResult.getType()).isEqualTo(expectedResult.getType());
        assertContentListDto(actualResult, expectedResult);
        assertContentsPage(actualResult.getContentsPage(), expectedContents);
    }

    private <T> void assertContentListDto(ContentReportDetailDto<T> actualResult,
                                          ContentReportDetailDto<T> expectedResult) {
        String type = actualResult.getType();
        if (type.equals(ContentType.POSTS.getDetailInKor())) {
            assertContentListDtoId(actualResult.getPostsListDto(), expectedResult.getPostsListDto(),
                    new PostsConverterForTest());
        } else if (type.equals(ContentType.COMMENTS.getDetailInKor())) {
            assertContentListDtoId(actualResult.getCommentsListDto(), expectedResult.getCommentsListDto(),
                    new CommentsConverterForTest<>(CommentsListDto.class));
        } else {
            assertContentListDtoId(actualResult.getUsersListDto(), expectedResult.getUsersListDto(),
                    new UserConverterForTest());
        }
    }

    private <T, U> void assertContentListDtoId(T actualContentListDto,
                                            T expectedContentListDto,
                                            EntityConverterForTest<T, U> entityConverter) {
        assertThat(entityConverter.extractDtoId(actualContentListDto))
                .isEqualTo(entityConverter.extractDtoId(expectedContentListDto));
    }

    private void assertContentsPage(ContentsPageDto<ContentReportDetailListDto> actualResult,
                                    Page<ContentReportDetailListDto> expectedContents) {
        ContentsPageDtoTestHelper<ContentReportDetailListDto, ContentReports> findHelper
                = ContentsPageDtoTestHelper.<ContentReportDetailListDto, ContentReports>builder()
                .actualResult(actualResult).expectedContents(expectedContents)
                .entityConverter(new ContentReportsConverterForTest()).build();
        findHelper.createExpectedResultAndAssertContentsPage();
    }
}
