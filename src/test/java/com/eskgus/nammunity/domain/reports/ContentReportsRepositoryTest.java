package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.comments.Comments;
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
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

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

    // TODO: reportsRepo.findOtherReasonById 테스트 수정 (findDetails() 수정 후)
    @Test
    public void findOtherReasonById() {
//        // 1. user1 회원가입 + user2 회원가입
//        User user1 = userRepository.findById(1L).get();
//        User user2 = userRepository.findById(2L).get();
//
//        // 2. user2가 user1 사용자 신고 * 3 (마지막 신고: 기타 사유)
//        Long latestReportId = saveReportsAndGetLatestReportId(testDB::saveUserReports, user1, user2);
//
//        // 3. latestReportId로 ContentReports 찾기
//        ContentReports report = getContentReportsById(latestReportId);
//
//        // 4. latestReportId로 findOtherReasonById() 호출
//        String actualOtherReason = contentReportsRepository.findOtherReasonById(latestReportId);
//
//        // 5. actualOtherReason이 report의 otherReasons랑 같은지 확인
//        Assertions.assertThat(actualOtherReason).isEqualTo(report.getOtherReasons());
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
    public void deleteByPosts() {
        // 1. user1 회원가입 + user2 회원가입
        User user2 = userRepository.findById(2L).get();

        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();

        // 3. user2가 user1이 작성한 게시글 신고 * 10
        saveReportsAndGetLatestReportId(testDB::savePostReports, post.getId(), user2);

        // 4. post로 deleteByPosts() 호출 + 검증
        callAndAssertDeleteByTypes(post, contentReportsRepository::deleteByPosts);
    }

    @Test
    public void deleteByComments() {
        // 1. user1 회원가입 + user2 회원가입
        User user2 = userRepository.findById(2L).get();

        // 2. user1이 게시글 작성
        // 3. user1이 댓글 작성
        Comments comment = commentsRepository.findById(1L).get();

        // 4. user2가 user1이 작성한 댓글 신고 * 10
        saveReportsAndGetLatestReportId(testDB::saveCommentReports, comment.getId(), user2);

        // 5. comment로 deleteByComments() 호출 + 검증
        callAndAssertDeleteByTypes(comment, contentReportsRepository::deleteByComments);
    }

    @Test
    public void deleteByUsers() {
        // 1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(1L).get();
        User user2 = userRepository.findById(2L).get();

        // 2. user2가 user1 사용자 신고 * 3
        saveReportsAndGetLatestReportId(testDB::saveUserReports, user1, user2);

        // 3. user1로 deleteByUsers() 호출 + 검증
        callAndAssertDeleteByTypes(user1, contentReportsRepository::deleteByUsers);
    }

    @Test
    public void countPostReportsByUser() {
        // 1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(1L).get();
        User user2 = userRepository.findById(2L).get();

        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();

        // 3. 게시글 신고 x 후 호출
        callAndAssertCountByUserInTypes(user1, contentReportsRepository::countPostReportsByUser);

        // 4. user2가 user1이 작성한 게시글 신고 * 10 후 호출
        saveReportsAndGetLatestReportId(testDB::savePostReports, post.getId(), user2);

        callAndAssertCountByUserInTypes(user1, contentReportsRepository::countPostReportsByUser);
    }

    @Test
    public void countCommentReportsByUser() {
        // 1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(1L).get();
        User user2 = userRepository.findById(2L).get();

        // 2. user1이 게시글 작성
        // 3. user1이 댓글 작성
        Comments comment = commentsRepository.findById(1L).get();

        // 4. 댓글 신고 x 후 호출
        callAndAssertCountByUserInTypes(user1, contentReportsRepository::countCommentReportsByUser);

        // 5. user2가 user1이 작성한 댓글 신고 * 10 후 호출
        saveReportsAndGetLatestReportId(testDB::saveCommentReports, comment.getId(), user2);

        callAndAssertCountByUserInTypes(user1, contentReportsRepository::countCommentReportsByUser);
    }

    @Test
    public void countUserReportsByUser() {
        // 1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(1L).get();
        User user2 = userRepository.findById(2L).get();

        // 2. 사용자 신고 x 후 호출
        callAndAssertCountByUserInTypes(user1, contentReportsRepository::countUserReportsByUser);

        // 3. user2가 user1 사용자 신고 * 3 후 호출
        saveReportsAndGetLatestReportId(testDB::saveUserReports, user1, user2);

        callAndAssertCountByUserInTypes(user1, contentReportsRepository::countUserReportsByUser);
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

    private <T> Long saveReportsAndGetLatestReportId(BiFunction<T, User, Long> saver,
                                                     T t, User user) {
        Long latestReportId = saver.apply(t, user);
        Assertions.assertThat(contentReportsRepository.count()).isEqualTo(latestReportId);
        return latestReportId;
    }

    private <T> void callAndAssertDeleteByTypes(T type, Consumer<T> deleter) {
        // 1. type으로 deleteBy@@() 호출
        deleter.accept(type);

        // 2. db에 남은 신고 수가 0인지 확인
        Assertions.assertThat(contentReportsRepository.count()).isZero();
    }

    private void callAndAssertCountByUserInTypes(User user, Function<User, Long> function) {
        // 1. expectedCount에 현재 저장된 (게시글/댓글/사용자) 신고 수 저장
        long expectedCount = contentReportsRepository.count();

        // 2. user로 function 호출
        long actualCount = function.apply(user);

        // 3. 리턴 값이 expectedCount랑 같은지 확인
        Assertions.assertThat(actualCount).isEqualTo(expectedCount);
    }

    private ContentReports getContentReportsById(Long id) {
        Optional<ContentReports> result = contentReportsRepository.findById(id);
        Assertions.assertThat(result).isPresent();
        return result.get();
    }
}
