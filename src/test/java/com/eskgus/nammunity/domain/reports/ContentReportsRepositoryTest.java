package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.reports.ContentReportDetailListDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ContentReportsRepositoryTest {
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

    private User user1;
    private User user2;
    private Posts post;
    private Comments comment;

    private ContentReports latestPostReport;
    private ContentReports latestCommentReport;
    private ContentReports latestUserReport;

    @BeforeEach
    public void setUp() {
        // 1. user1 회원가입 + user2 회원가입
        Long user1Id = testDB.signUp(1L, Role.USER);
        Long user2Id = testDB.signUp(2L, Role.USER);
        Assertions.assertThat(userRepository.count()).isEqualTo(user2Id);

        this.user1 = userRepository.findById(user1Id).get();
        this.user2 = userRepository.findById(user2Id).get();

        // 2. user1이 게시글 작성
        Long postId = testDB.savePosts(this.user1);
        Assertions.assertThat(postsRepository.count()).isEqualTo(postId);

        this.post = postsRepository.findById(postId).get();

        // 3. user1이 댓글 작성
        Long commentId = testDB.saveComments(postId, this.user1);
        Assertions.assertThat(commentsRepository.count()).isEqualTo(commentId);

        this.comment = commentsRepository.findById(commentId).get();
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void findReporterByContents() {
        // 게시글 신고 + 댓글 신고 + 사용자 신고
        saveReports();

        callAndAssertFindReporterByContents(this.post, this.latestPostReport);
        callAndAssertFindReporterByContents(this.comment, this.latestCommentReport);
        callAndAssertFindReporterByContents(this.user1, this.latestUserReport);
    }

    private void saveReports() {
        Long postReportId = testDB.savePostReports(this.post.getId(), this.user2);
        Long commentReportId = testDB.saveCommentReports(this.comment.getId(), this.user2);
        Long userReportId = testDB.saveUserReports(this.user1, this.user2);
        Assertions.assertThat(contentReportsRepository.count()).isEqualTo(userReportId);

        this.latestPostReport = contentReportsRepository.findById(postReportId).get();
        this.latestCommentReport = contentReportsRepository.findById(commentReportId).get();
        this.latestUserReport = contentReportsRepository.findById(userReportId).get();
    }

    private <T> void callAndAssertFindReporterByContents(T contents, ContentReports expectedReport) {
        User actualReporter = contentReportsRepository.findReporterByContents(contents);
        Assertions.assertThat(actualReporter.getId()).isEqualTo(expectedReport.getReporter().getId());
    }

    @Test
    public void findReportedDateByContents() {
        saveReports();

        callAndAssertFindReportedDateByContents(this.post, this.latestPostReport);
        callAndAssertFindReportedDateByContents(this.comment, this.latestCommentReport);
        callAndAssertFindReportedDateByContents(this.user1, this.latestUserReport);
    }

    private <T> void callAndAssertFindReportedDateByContents(T contents, ContentReports expectedReport) {
        LocalDateTime actualReportedDate = contentReportsRepository.findReportedDateByContents(contents);
        Assertions.assertThat(actualReportedDate).isEqualTo(expectedReport.getCreatedDate());
    }

    @Test
    public void findReasonByContents() {
        saveReports();

        callAndAssertFindReasonByContents(this.post, this.latestPostReport);
        callAndAssertFindReasonByContents(this.comment, this.latestCommentReport);
        callAndAssertFindReasonByContents(this.user1, this.latestUserReport);
    }

    public <T> void callAndAssertFindReasonByContents(T contents, ContentReports expectedReport) {
        Reasons actualReason = contentReportsRepository.findReasonByContents(contents);
        Assertions.assertThat(actualReason.getId()).isEqualTo(expectedReport.getReasons().getId());
    }

    @Test
    public void findOtherReasonByContents() {
        saveReportsWithOtherReason();

        callAndAssertFindOtherReasonByContents(this.post, this.latestPostReport);
        callAndAssertFindOtherReasonByContents(this.comment, this.latestCommentReport);
        callAndAssertFindOtherReasonByContents(this.user1, this.latestUserReport);
    }

    private void saveReportsWithOtherReason() {
        String[] otherReasons = { "기타 사유 1", "기타 사유 2", "기타 사유 3" };
        Long postReportId = 0L;
        Long commentReportId = 0L;
        Long userReportId = 0L;
        for (String otherReason : otherReasons) {
            postReportId = testDB.savePostReportsWithOtherReason(this.post.getId(), this.user2, otherReason);
            commentReportId = testDB.saveCommentReportsWithOtherReason(this.comment.getId(), this.user2, otherReason);
            userReportId = testDB.saveUserReportsWithOtherReason(this.user1, this.user2, otherReason);
        }
        Assertions.assertThat(contentReportsRepository.count()).isEqualTo(userReportId);

        this.latestPostReport = contentReportsRepository.findById(postReportId).get();
        this.latestCommentReport = contentReportsRepository.findById(commentReportId).get();
        this.latestUserReport = contentReportsRepository.findById(userReportId).get();
    }

    private <T> void callAndAssertFindOtherReasonByContents(T contents, ContentReports expectedReport) {
        Reasons reason = expectedReport.getReasons();
        String actualOtherReason = contentReportsRepository.findOtherReasonByContents(contents, reason);
        Assertions.assertThat(actualOtherReason).isEqualTo(expectedReport.getOtherReasons());
    }

    @Test
    public void findByContents() {
        saveReports();

        callAndAssertFindByContents(this.post, this.latestPostReport);
        callAndAssertFindByContents(this.comment, this.latestCommentReport);
        callAndAssertFindByContents(this.user1, this.latestUserReport);
    }

    private <T> void callAndAssertFindByContents(T contents, ContentReports expectedLatestReport) {
        List<ContentReportDetailListDto> expectedReports = getExpectedReports(expectedLatestReport);
        List<ContentReportDetailListDto> actualReports = contentReportsRepository.findByContents(contents);

        Assertions.assertThat(actualReports.size()).isEqualTo(expectedReports.size());
        for (int i = 0; i < actualReports.size(); i++) {
            Assertions.assertThat(actualReports.get(i).getId()).isEqualTo(expectedReports.get(i).getId());
        }
    }

    private List<ContentReportDetailListDto> getExpectedReports(ContentReports expectedLatestReport) {
        long startIndex = getStartIndex(expectedLatestReport) + 1;
        long endIndex = expectedLatestReport.getId();
        List<ContentReportDetailListDto> reports = new ArrayList<>();

        for (long id = startIndex; id <= endIndex; id++) {
            ContentReports report = contentReportsRepository.findById(id).get();
            ContentReportDetailListDto detailListDto = ContentReportDetailListDto.builder().report(report).build();
            reports.add(detailListDto);
        }
        return reports;
    }

    private long getStartIndex(ContentReports expectedLatestReport) {
        String type = expectedLatestReport.getTypes().getDetail();

        if (type.equals("게시글")) {
            return 0;
        } else if (type.equals("댓글")) {
            return this.latestPostReport.getId();
        }
        return this.latestCommentReport.getId();
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
        Long postReportId = saveReportsAndGetLatestReportId(testDB::savePostReports, post.getId(), user2);
        callAndAssertCountReportsByContentTypeAndUser(postType, postReportId);

        // 2. 댓글
        // 2-1. 댓글 신고 x 후 호출
        callAndAssertCountReportsByContentTypeAndUser(commentType, 0);
        // 2-2. 댓글 신고 후 호출
        Long commentReportId = saveReportsAndGetLatestReportId(testDB::saveCommentReports, comment.getId(), user2);
        callAndAssertCountReportsByContentTypeAndUser(commentType, commentReportId - postReportId);


        // 3. 사용자
        // 3-1. 사용자 신고 x 후 호출
        callAndAssertCountReportsByContentTypeAndUser(userType, 0);
        // 3-2. 사용자 신고 후 호출
        Long userReportId = saveReportsAndGetLatestReportId(testDB::saveUserReports, user1, user2);
        callAndAssertCountReportsByContentTypeAndUser(userType, userReportId - commentReportId);
    }

    private <T> void callAndAssertCountReportsByContentTypeAndUser(ContentType contentType, long expectedCount) {
        long actualCount = contentReportsRepository.countReportsByContentTypeAndUser(contentType, user1);
        Assertions.assertThat(actualCount).isEqualTo(expectedCount);
    }

    private <T> Long saveReportsAndGetLatestReportId(BiFunction<T, User, Long> saver, T t, User user) {
        Long latestReportId = saver.apply(t, user);
        Assertions.assertThat(contentReportsRepository.count()).isEqualTo(latestReportId);
        return latestReportId;
    }

    @Test
    public void countByContents() {
        // 1. 신고 x 후 호출
        callAndAssertCountByContents(this.post, 0L);
        callAndAssertCountByContents(this.comment, 0L);
        callAndAssertCountByContents(this.user1, 0L);

        // 2. 신고 후 호출
        saveReports();

        callAndAssertCountByContents(this.post, this.latestPostReport.getId());
        callAndAssertCountByContents(this.comment,
                this.latestCommentReport.getId() - this.latestPostReport.getId());
        callAndAssertCountByContents(this.user1,
                this.latestUserReport.getId() - this.latestCommentReport.getId());
    }

    private <T> void callAndAssertCountByContents(T contents, long expectedCountByContents) {
        long actualCountByContents = contentReportsRepository.countByContents(contents);
        Assertions.assertThat(actualCountByContents).isEqualTo(expectedCountByContents);
    }
}
