package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.reports.*;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.reports.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

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
    private ReportsService reportsService;

    private User user1;
    private User user2;
    private Posts post;
    private Comments comment;

    private Long latestPostReportId;
    private Long latestCommentReportId;
    private Long latestUserReportId;

    private ContentType contentType;

    @BeforeEach
    public void setUp() {
        // 1. user1 회원가입 + user2 (관리자) 회원가입
        Long user1Id = testDB.signUp(1L, Role.USER);
        Long user2Id = testDB.signUp(2L, Role.ADMIN);
        assertThat(userRepository.count()).isEqualTo(user2Id);

        this.user1 = userRepository.findById(user1Id).get();
        this.user2 = userRepository.findById(user2Id).get();

        // 2. user1이 게시글 작성
        Long postId = testDB.savePosts(user1);
        assertThat(postsRepository.count()).isEqualTo(postId);

        this.post = postsRepository.findById(postId).get();

        // 3. user1이 댓글 작성
        Long commentId = testDB.saveComments(postId, user1);
        assertThat(commentsRepository.count()).isEqualTo(commentId);

        this.comment = commentsRepository.findById(commentId).get();
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void saveContentReports() {
        // 1. 최초 신고 (신고 요약 저장 x)
        callAndAssertSaveContentReports(post, user2);
        callAndAssertSaveContentReports(comment, user2);
        callAndAssertSaveContentReports(user1, user2);
        assertThat(contentReportSummaryRepository.count()).isZero();

        // 2. 누적 신고 10 or 3개 이상 (신고 요약 저장 o)
        saveReports();

        callAndAssertSaveContentReports(post, user2);
        callAndAssertSaveContentReports(comment, user2);
        callAndAssertSaveContentReports(user1, user2);
        assertThat(contentReportSummaryRepository.count()).isEqualTo(latestUserReportId - latestCommentReportId);
    }

    private <T> void callAndAssertSaveContentReports(T contents, User reporter) {
        Long reportId = callSaveContentReports(contents, reporter);
        assertSaveContentReports(contents, reporter, reportId);
    }

    private <T> Long callSaveContentReports(T contents, User reporter) {
        ContentReportsSaveDto requestDto = createReportsSaveDto(contents);
        return reportsService.saveContentReports(requestDto, reporter.getUsername());
    }

    private <T> ContentReportsSaveDto createReportsSaveDto(T contents) {
        Long postId = contents instanceof Posts ? ((Posts) contents).getId() : null;
        Long commentId = contents instanceof Comments ? ((Comments) contents).getId() : null;
        Long userId = contents instanceof User ? ((User) contents).getId() : null;
        Long reasonId = reasonsRepository.count();
        String otherReason = "기타 사유";

        ContentReportsSaveDto saveDto = new ContentReportsSaveDto();
        saveDto.setPostsId(postId);
        saveDto.setCommentsId(commentId);
        saveDto.setUserId(userId);
        saveDto.setReasonsId(reasonId);
        saveDto.setOtherReasons(otherReason);

        return saveDto;
    }

    private <T> void assertSaveContentReports(T contents, User reporter, Long reportId) {
        Optional<ContentReports> result = contentReportsRepository.findById(reportId);
        assertThat(result).isPresent();

        ContentReports report = result.get();
        assertThat(report.getReporter().getId()).isEqualTo(reporter.getId());
        assertContentId(report, contents);
    }

    private <T> void assertContentId(ContentReports report, T contents) {
        Long actualId = getContentIdFromReport(report);
        Long expectedId = getIdFromContent(contents);
        assertThat(actualId).isEqualTo(expectedId);
    }

    private Long getContentIdFromReport(ContentReports report) {
        String type = report.getTypes().getDetail();
        if (type.equals(ContentType.POSTS.getDetail())) {
            return report.getPosts().getId();
        } else if (type.equals(ContentType.COMMENTS.getDetail())) {
            return report.getComments().getId();
        }
        return report.getUser().getId();
    }

    private <T> Long getIdFromContent(T contents) {
        if (contents instanceof Posts) {
            return ((Posts) contents).getId();
        } else if (contents instanceof Comments) {
            return ((Comments) contents).getId();
        }
        return ((User) contents).getId();
    }

    private void saveReports() {
        this.latestPostReportId = testDB.savePostReports(post.getId(), user2);
        this.latestCommentReportId = testDB.saveCommentReports(comment.getId(), user2);
        this.latestUserReportId = testDB.saveUserReports(user1, user2);
        assertThat(contentReportsRepository.count()).isEqualTo(latestUserReportId);
    }

    @Test
    public void findDetails() {
        saveReports();

        // 1. contentType = post, id = post의 id
        callAndAssertFindDetails(ContentType.POSTS, post);

        // 2. contentType = comment, id = comment의 id
        callAndAssertFindDetails(ContentType.COMMENTS, comment);

        // 3. contentType = user, id = user1의 id
        callAndAssertFindDetails(ContentType.USERS, user1);
    }

    private <T> void callAndAssertFindDetails(ContentType contentType, T content) {
        this.contentType = contentType;

        Long contentId = getIdFromContent(content);
        ContentReportDetailDto detailDto = reportsService.findDetails(contentType, contentId);

        assertFindDetails(detailDto);
    }

    private void assertFindDetails(ContentReportDetailDto detailDto) {
        assertDetailDto(detailDto);
        assertDetailListDtos(detailDto.getReports());
    }

    private void assertDetailDto(ContentReportDetailDto detailDto) {
        boolean isContentReport;
        String expectedType;
        long expectedNumberOfReports;
        if (contentType.equals(ContentType.POSTS)) {
            isContentReport = detailDto.isPostReport();
            expectedType = ContentType.POSTS.getDetail();
            expectedNumberOfReports = latestPostReportId.intValue();
        } else if (contentType.equals(ContentType.COMMENTS)) {
            isContentReport = detailDto.isCommentReport();
            expectedType = ContentType.COMMENTS.getDetail();
            expectedNumberOfReports = latestCommentReportId - latestPostReportId;
        } else {
            isContentReport = detailDto.isUserReport();
            expectedType = ContentType.USERS.getDetail();
            expectedNumberOfReports = latestUserReportId - latestCommentReportId;
        }

        assertThat(isContentReport).isTrue();
        assertThat(detailDto.getType()).isEqualTo(expectedType);
        assertThat(detailDto.getNumOfReports()).isEqualTo(expectedNumberOfReports);
    }

    private void assertDetailListDtos(List<ContentReportDetailListDto> detailListDtos) {
        List<Long> expectedListDtoIds = getExpectedListDtoIds();
        assertThat(detailListDtos).extracting(ContentReportDetailListDto::getId).isEqualTo(expectedListDtoIds);
    }

    private List<Long> getExpectedListDtoIds() {
        long startIndex;
        long endIndex;
        if (contentType.equals(ContentType.POSTS)) {
            startIndex = 0;
            endIndex = latestPostReportId;
        } else if (contentType.equals(ContentType.COMMENTS)) {
            startIndex = latestPostReportId;
            endIndex = latestCommentReportId;
        } else {
            startIndex = latestCommentReportId;
            endIndex = latestUserReportId;
        }

        List<Long> expectedListDtoIds = new ArrayList<>();
        for (long i = startIndex + 1; i < endIndex + 1; i++) {
            expectedListDtoIds.add(i);
        }
        return expectedListDtoIds;
    }
}
