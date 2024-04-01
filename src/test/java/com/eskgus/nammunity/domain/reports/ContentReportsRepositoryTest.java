package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.converter.ContentReportsConverterForTest;
import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.helper.FindHelperForTest;
import com.eskgus.nammunity.helper.repository.finder.RepositoryBiFinderForTest;
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
import java.util.function.BiFunction;

import static com.eskgus.nammunity.util.FindUtilForTest.callAndAssertFind;
import static com.eskgus.nammunity.util.FindUtilForTest.initializeFindHelper;

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
        Long postId = testDB.savePosts(user1);
        Assertions.assertThat(postsRepository.count()).isEqualTo(postId);

        this.post = postsRepository.findById(postId).get();

        // 3. user1이 댓글 작성
        Long commentId = testDB.saveComments(postId, user1);
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

        callAndAssertFindReporterByContents(post, latestPostReport);
        callAndAssertFindReporterByContents(comment, latestCommentReport);
        callAndAssertFindReporterByContents(user1, latestUserReport);
    }

    private void saveReports() {
        Long postReportId = testDB.savePostReports(post.getId(), user2);
        Long commentReportId = testDB.saveCommentReports(comment.getId(), user2);
        Long userReportId = testDB.saveUserReports(user1, user2);
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

        callAndAssertFindReportedDateByContents(post, latestPostReport);
        callAndAssertFindReportedDateByContents(comment, latestCommentReport);
        callAndAssertFindReportedDateByContents(user1, latestUserReport);
    }

    private <T> void callAndAssertFindReportedDateByContents(T contents, ContentReports expectedReport) {
        LocalDateTime actualReportedDate = contentReportsRepository.findReportedDateByContents(contents);
        Assertions.assertThat(actualReportedDate).isEqualTo(expectedReport.getCreatedDate());
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
        Assertions.assertThat(actualReason.getId()).isEqualTo(expectedReport.getReasons().getId());
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
            postReportId = testDB.savePostReportsWithOtherReason(post.getId(), user2, otherReason);
            commentReportId = testDB.saveCommentReportsWithOtherReason(comment.getId(), user2, otherReason);
            userReportId = testDB.saveUserReportsWithOtherReason(user1, user2, otherReason);
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

        callAndAssertFindByContents(post);
        callAndAssertFindByContents(comment);
        callAndAssertFindByContents(user1);
    }

    private <T> void callAndAssertFindByContents(T contents) {
        FindHelperForTest<RepositoryBiFinderForTest<ContentReportDetailListDto, T>,
                            ContentReports, ContentReportDetailListDto, T> findHelper = createBiFindHelper(contents);
        callAndAssertFindDetailListDto(findHelper);
    }

    private <T> FindHelperForTest<RepositoryBiFinderForTest<ContentReportDetailListDto, T>,
                                    ContentReports, ContentReportDetailListDto, T> createBiFindHelper(T contents) {
        EntityConverterForTest<ContentReports, ContentReportDetailListDto> entityConverter
                = new ContentReportsConverterForTest();
        return FindHelperForTest.<RepositoryBiFinderForTest<ContentReportDetailListDto,T>, ContentReports, ContentReportDetailListDto, T>builder()
                .finder(contentReportsRepository::findByContents)
                .contents(contents)
                .entityStream(contentReportsRepository.findAll().stream())
                .page(1).limit(3)
                .entityConverter(entityConverter).build();
    }

    private <T> void
        callAndAssertFindDetailListDto(FindHelperForTest<RepositoryBiFinderForTest<ContentReportDetailListDto, T>,
                                        ContentReports, ContentReportDetailListDto, T> findHelper) {
        initializeFindHelper(findHelper);
        callAndAssertFind();
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

    private void callAndAssertCountReportsByContentTypeAndUser(ContentType contentType, long expectedCount) {
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
        Assertions.assertThat(actualCountByContents).isEqualTo(expectedCountByContents);
    }
}
