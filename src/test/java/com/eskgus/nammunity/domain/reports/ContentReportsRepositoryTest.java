package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.reports.ContentReportDistinctDto;
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
import java.util.function.Supplier;

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

    @BeforeEach
    public void setUp() {
        // 1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(testDB.signUp(1L, Role.USER)).get();
        testDB.signUp(2L, Role.USER);
        Assertions.assertThat(userRepository.count()).isEqualTo(2);

        // 2. user1이 게시글 작성
        Long postId = testDB.savePosts(user1);
        Assertions.assertThat(postsRepository.count()).isOne();

        // 3. user1이 댓글 작성
        testDB.saveComments(postId, user1);
        Assertions.assertThat(commentsRepository.count()).isOne();
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void findDistinct() {
        // 1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(1L).get();
        User user2 = userRepository.findById(2L).get();

        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();

        // 3. user1이 댓글 작성
        Comments comment = commentsRepository.findById(1L).get();

        // 4. user2가 user1이 작성한 게시글 신고 * 10, 댓글 신고 * 10, user1 사용자 신고 * 3
        saveReportsForFindingDistinctByTypes(post, comment, user1, user2);

        // 5. findDistinct() 호출 + 검증
        String[] expectedTypes = { "게시글", "댓글", "사용자" };
        Long[] expectedIds = { post.getId(), comment.getId(), user1.getId() };
        callAndAssertFindDistinctByTypes(contentReportsRepository::findDistinct, 3, expectedTypes, expectedIds);
    }

    @Test
    public void findDistinctByPosts() {
        // 1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(1L).get();
        User user2 = userRepository.findById(2L).get();

        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();

        // 3. user1이 댓글 작성
        Comments comment = commentsRepository.findById(1L).get();

        // 4. user2가 user1이 작성한 게시글 신고 * 10, 댓글 신고 * 10, user1 사용자 신고 * 3
        saveReportsForFindingDistinctByTypes(post, comment, user1, user2);

        // 5. findDistinctByPosts() 호출 + 검증
        String[] expectedTypes = { "게시글" };
        Long[] expectedIds = { post.getId() };
        callAndAssertFindDistinctByTypes(contentReportsRepository::findDistinctByPosts, 1, expectedTypes, expectedIds);
    }

    @Test
    public void findDistinctByComments() {
        // 1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(1L).get();
        User user2 = userRepository.findById(2L).get();

        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();

        // 3. user1이 댓글 작성
        Comments comment = commentsRepository.findById(1L).get();

        // 4. user2가 user1이 작성한 게시글 신고 * 10, 댓글 신고 * 10, user1 사용자 신고 * 3
        saveReportsForFindingDistinctByTypes(post, comment, user1, user2);

        // 5. findDistinctByComments() 호출
        String[] expectedTypes = { "댓글" };
        Long[] expectedIds = { comment.getId() };
        callAndAssertFindDistinctByTypes(contentReportsRepository::findDistinctByComments, 1, expectedTypes, expectedIds);
    }

    @Test
    public void findDistinctByUsers() {
        // 1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(1L).get();
        User user2 = userRepository.findById(2L).get();

        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();

        // 3. user1이 댓글 작성
        Comments comment = commentsRepository.findById(1L).get();

        // 4. user2가 user1이 작성한 게시글 신고 * 10, 댓글 신고 * 10, user1 사용자 신고 * 3
        saveReportsForFindingDistinctByTypes(post, comment, user1, user2);

        // 5. findDistinctByUsers() 호출
        String[] expectedTypes = { "사용자" };
        Long[] expectedIds = { user1.getId() };
        callAndAssertFindDistinctByTypes(contentReportsRepository::findDistinctByUsers, 1, expectedTypes, expectedIds);
    }

    @Test
    public void findReporterByPosts() {
        // 1. user1 회원가입 + user2 회원가입
        User user2 = userRepository.findById(2L).get();

        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();

        // 3. user2가 user1이 작성한 게시글 신고 * 10
        saveReportsAndGetLatestReportId(testDB::savePostReports, post.getId(), user2);

        // 4. post로 findReporterByPosts() 호출 + 검증
        callAndAssertFindReporterByTypes(post, contentReportsRepository::findReporterByPosts, user2);
    }

    @Test
    public void findReporterByComments() {
        // 1. user1 회원가입 + user2 회원가입
        User user2 = userRepository.findById(2L).get();

        // 2. user1이 게시글 작성
        // 3. user1이 댓글 작성
        Comments comment = commentsRepository.findById(1L).get();

        // 4. user2가 user1이 작성한 댓글 신고 * 10
        saveReportsAndGetLatestReportId(testDB::saveCommentReports, comment.getId(), user2);

        // 5. comment로 findReporterByComments() 호출 + 검증
        callAndAssertFindReporterByTypes(comment, contentReportsRepository::findReporterByComments, user2);
    }

    @Test
    public void findReporterByUsers() {
        // 1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(1L).get();
        User user2 = userRepository.findById(2L).get();

        // 2. user2가 user1 사용자 신고 * 3
        saveReportsAndGetLatestReportId(testDB::saveUserReports, user1, user2);

        // 3. user1로 findReporterByUsers() 호출 + 검증
        callAndAssertFindReporterByTypes(user1, contentReportsRepository::findReporterByUsers, user2);
    }

    @Test
    public void findReportedDateByPosts() {
        // 1. user1 회원가입 + user2 회원가입
        User user2 = userRepository.findById(2L).get();

        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();

        // 3. user2가 user1이 작성한 게시글 신고 * 10
        Long latestReportId = saveReportsAndGetLatestReportId(testDB::savePostReports, post.getId(), user2);

        // 4. post로 findReportedDateByPosts() 호출 + 검증
        callAndAssertFindReportedDateByTypes(post, contentReportsRepository::findReportedDateByPosts, latestReportId);
    }

    @Test
    public void findReportedDateByComments() {
        // 1. user1 회원가입 + user2 회원가입
        User user2 = userRepository.findById(2L).get();

        // 2. user1이 게시글 작성
        // 3. user1이 댓글 작성
        Comments comment = commentsRepository.findById(1L).get();

        // 4. user2가 user1이 작성한 댓글 신고 * 10
        Long latestReportId = saveReportsAndGetLatestReportId(testDB::saveCommentReports, comment.getId(), user2);

        // 5. comment로 findReportedDateByComments() 호출 + 검증
        callAndAssertFindReportedDateByTypes(comment, contentReportsRepository::findReportedDateByComments, latestReportId);
    }

    @Test
    public void findReportedDateByUsers() {
        // 1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(1L).get();
        User user2 = userRepository.findById(2L).get();

        // 2. user2가 user1 사용자 신고 * 3
        Long latestReportId = saveReportsAndGetLatestReportId(testDB::saveUserReports, user1, user2);

        // 3. user1로 findReportedDateByUsers() 호출 + 검증
        callAndAssertFindReportedDateByTypes(user1, contentReportsRepository::findReportedDateByUsers, latestReportId);
    }

    @Test
    public void findReasonByPosts() {
        // 1. user1 회원가입 + user2 회원가입
        User user2 = userRepository.findById(2L).get();

        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();

        // 3. user2가 user1이 작성한 게시글 신고 * 10
        Long latestReportId = saveReportsAndGetLatestReportId(testDB::savePostReports, post.getId(), user2);

        // 4. post로 findReasonByPosts() 호출 + 검증
        callAndAssertFindReasonByTypes(post, contentReportsRepository::findReasonByPosts, latestReportId);
    }

    @Test
    public void findReasonByComments() {
        // 1. user1 회원가입 + user2 회원가입
        User user2 = userRepository.findById(2L).get();

        // 2. user1이 게시글 작성
        // 3. user1이 댓글 작성
        Comments comment = commentsRepository.findById(1L).get();

        // 4. user2가 user1이 작성한 댓글 신고 * 10
        Long latestReportId = saveReportsAndGetLatestReportId(testDB::saveCommentReports, comment.getId(), user2);

        // 5. comment로 findReasonByComments() 호출 + 검증
        callAndAssertFindReasonByTypes(comment, contentReportsRepository::findReasonByComments, latestReportId);
    }

    @Test
    public void findReasonByUsers() {
        // 1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(1L).get();
        User user2 = userRepository.findById(2L).get();

        // 2. user2가 user1 사용자 신고 * 3
        Long latestReportId = saveReportsAndGetLatestReportId(testDB::saveUserReports, user1, user2);

        // 3. user1로 findReasonByUsers() 호출 + 검증
        callAndAssertFindReasonByTypes(user1, contentReportsRepository::findReasonByUsers, latestReportId);
    }

    @Test
    public void findOtherReasonByPosts() {
        // 1. user1 회원가입 + user2 회원가입
        User user2 = userRepository.findById(2L).get();

        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();

        // 3. user2가 user1이 작성한 게시글 기타 사유로 신고 * 3
        String[] otherReasons = { "기타 사유 1", "기타 사유 2", "기타 사유 3" };
        Long latestReportId = null;
        for (String otherReason : otherReasons) {
            latestReportId = testDB.savePostReportsWithOtherReason(post.getId(), user2, otherReason);
        }
        Assertions.assertThat(contentReportsRepository.count()).isEqualTo(latestReportId);

        // 4. post, reason(기타)로 findOtherReasonByPosts() 호출 + 검증
        callAndAssertFindOtherReasonByTypes(post, contentReportsRepository::findOtherReasonByPosts, latestReportId);
    }

    @Test
    public void findOtherReasonByComments() {
        // 1. user1 회원가입 + user2 회원가입
        User user2 = userRepository.findById(2L).get();

        // 2. user1이 게시글 작성
        // 3. user1이 댓글 작성
        Comments comment = commentsRepository.findById(1L).get();

        // 4. user2가 user1이 작성한 댓글 기타 사유로 신고 * 3
        String[] otherReasons = { "기타 사유 1", "기타 사유 2", "기타 사유 3" };
        Long latestReportId = null;
        for (String otherReason : otherReasons) {
            latestReportId = testDB.saveCommentReportsWithOtherReason(comment.getId(), user2, otherReason);
        }
        Assertions.assertThat(contentReportsRepository.count()).isEqualTo(latestReportId);

        // 5. comment, reason(기타)로 findOtherReasonByComments() 호출 + 검증
        callAndAssertFindOtherReasonByTypes(comment, contentReportsRepository::findOtherReasonByComments, latestReportId);
    }

    @Test
    public void findOtherReasonByUsers() {
        // 1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(1L).get();
        User user2 = userRepository.findById(2L).get();

        // 2. user2가 user1 사용자 기타 사유로 신고 * 3
        String[] otherReasons = { "기타 사유 1", "기타 사유 2", "기타 사유 3" };
        Long latestReportId = null;
        for (String otherReason : otherReasons) {
            latestReportId = testDB.saveUserReportsWithOtherReason(user1, user2, otherReason);
        }
        Assertions.assertThat(contentReportsRepository.count()).isEqualTo(latestReportId);

        // 3. user1, reason(기타)로 findOtherReasonByUsers() 호출 + 검증
        callAndAssertFindOtherReasonByTypes(user1, contentReportsRepository::findOtherReasonByUsers, latestReportId);
    }

    @Test
    public void findOtherReasonById() {
        // 1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(1L).get();
        User user2 = userRepository.findById(2L).get();

        // 2. user2가 user1 사용자 신고 * 3 (마지막 신고: 기타 사유)
        Long latestReportId = saveReportsAndGetLatestReportId(testDB::saveUserReports, user1, user2);

        // 3. latestReportId로 ContentReports 찾기
        ContentReports report = getContentReportsById(latestReportId);

        // 4. latestReportId로 findOtherReasonById() 호출
        String actualOtherReason = contentReportsRepository.findOtherReasonById(latestReportId);

        // 5. actualOtherReason이 report의 otherReasons랑랑 같은지 확인
        Assertions.assertThat(actualOtherReason).isEqualTo(report.getOtherReasons());
    }

    @Test
    public void findByPosts() {
        // 1. user1 회원가입 + user2 회원가입
        User user2 = userRepository.findById(2L).get();

        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();

        // 3. user2가 user1이 작성한 게시글 신고 * 10
        saveReportsAndGetLatestReportId(testDB::savePostReports, post.getId(), user2);

        // 5. post로 findByPosts() 호출 + 검증
        callAndAssertFindByTypes(post, contentReportsRepository::findByPosts, post.getId());
    }

    @Test
    public void findByComments() {
        // 1. user1 회원가입 + user2 회원가입
        User user2 = userRepository.findById(2L).get();

        // 2. user1이 게시글 작성
        // 3. user1이 댓글 작성
        Comments comment = commentsRepository.findById(1L).get();

        // 4. user2가 user1이 작성한 댓글 신고 * 10
        saveReportsAndGetLatestReportId(testDB::saveCommentReports, comment.getId(), user2);

        // 5. comment로 findByComments() 호출 + 검증
        callAndAssertFindByTypes(comment, contentReportsRepository::findByComments, comment.getId());
    }

    @Test
    public void findByUsers() {
        // 1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(1L).get();
        User user2 = userRepository.findById(2L).get();

        // 2. user2가 user1 사용자 신고 * 3
        saveReportsAndGetLatestReportId(testDB::saveUserReports, user1, user2);

        // 3. user1로 findByUser() 호출 + 검증
        callAndAssertFindByTypes(user1, contentReportsRepository::findByUsers, user1.getId());
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

    private void saveReportsForFindingDistinctByTypes(Posts post, Comments comment, User user, User reporter) {
        testDB.savePostReports(post.getId(), reporter);
        testDB.saveCommentReports(comment.getId(), reporter);
        Long latestReportId = testDB.saveUserReports(user, reporter);
        Assertions.assertThat(contentReportsRepository.count()).isEqualTo(latestReportId);
    }

    private <T> Long saveReportsAndGetLatestReportId(BiFunction<T, User, Long> saver,
                                                     T t, User user) {
        Long latestReportId = saver.apply(t, user);
        Assertions.assertThat(contentReportsRepository.count()).isEqualTo(latestReportId);
        return latestReportId;
    }

    private void callAndAssertFindDistinctByTypes(Supplier<List<ContentReportDistinctDto>> finder, int expectedSize,
                                                  String[] expectedTypes, Long[] expectedIds) {
        // 1. findDistinctBy@@() 호출
        List<ContentReportDistinctDto> result = finder.get();

        // 2. result의 size가 expectedSize인지 확인
        Assertions.assertThat(result.size()).isEqualTo(expectedSize);

        for (int i = 0; i < expectedSize; i++) {
            ContentReportDistinctDto distinctDto = result.get(i);

            // 3. result의 type 확인
            Assertions.assertThat(distinctDto.getTypes().getDetail()).isEqualTo(expectedTypes[i]);

            // 4. result의 posts/comments/user 확인
            Long actualId = switch (expectedTypes[i]) {
                case "게시글" -> distinctDto.getPosts().getId();
                case "댓글" -> distinctDto.getComments().getId();
                default -> distinctDto.getUser().getId();
            };
            Assertions.assertThat(actualId).isEqualTo(expectedIds[i]);
        }
    }

    private <T> void callAndAssertFindReporterByTypes(T type, Function<T, User> finder, User expectedReporter) {
        // 1. type으로 findReporterBy@@() 호출
        User actualReporter = finder.apply(type);

        // 2. actualReporter가 expectedReporter인지 확인
        Assertions.assertThat(actualReporter.getId()).isEqualTo(expectedReporter.getId());
    }

    private <T> void callAndAssertFindReportedDateByTypes(T type, Function<T, LocalDateTime> finder,
                                                          Long latestReportId) {
        // 1. latestReportId로 ContentReports 찾기
        ContentReports report = getContentReportsById(latestReportId);

        // 2. report의 createdDate 가져와서 expectedReportedDate에 저장
        LocalDateTime expectedReportedDate = report.getCreatedDate();

        // 3. type으로 findReportedDateBy@@() 호출
        LocalDateTime actualReportedDate = finder.apply(type);

        // 4. actualReportedDate가 expectedReportedDate랑 같은지 확인
        Assertions.assertThat(actualReportedDate).isEqualTo(expectedReportedDate);
    }

    public <T> void callAndAssertFindReasonByTypes(T type, Function<T, Reasons> finder, Long latestReportId) {
        // 1. latestReportId로 ContentReports 찾기
        ContentReports report = getContentReportsById(latestReportId);

        // 2. report의 reason 가져와서 expectedReason에 저장
        Reasons expectedReason = report.getReasons();

        // 3. type으로 findReasonBy@@() 호출
        Reasons actualReason = finder.apply(type);

        // 4. actualReason이 expectedReason이랑 같은지 확인
        Assertions.assertThat(actualReason.getId()).isEqualTo(expectedReason.getId());
    }

    private <T> void callAndAssertFindOtherReasonByTypes(T type, BiFunction<T, Reasons, String> finder,
                                                            Long latestReportId) {
        // 1. latestReportId로 ContentReports 찾기
        ContentReports report = getContentReportsById(latestReportId);

        // 2. report의 otherReason 가져와서 expectedOtherReason에 저장
        String expectedOtherReason = report.getOtherReasons();

        // 3. type, report의 reasons로 findOtherReasonBy@@() 호출
        String actualOtherReason = finder.apply(type, report.getReasons());

        // 4. actualOtherReason이 expectedOtherReason이랑 같은지 확인
        Assertions.assertThat(actualOtherReason).isEqualTo(expectedOtherReason);
    }

    private <T> void callAndAssertFindByTypes(T type, Function<T, List<ContentReports>> finder,
                                              Long expectedContentId) {
        // 1. expectedIdList 저장
        List<Long> expectedIdList = new ArrayList<>();
        for (long i = 1; i <= contentReportsRepository.count(); i++) {
            expectedIdList.add(i);
        }

        // 2. type으로 findBy@@() 호출
        List<ContentReports> result = finder.apply(type);
        Assertions.assertThat(result.size()).isEqualTo(expectedIdList.size());

        // 3. result의 각 id가 expectedIdList랑 같은지 확인
        Assertions.assertThat(result).extracting(ContentReports::getId).isEqualTo(expectedIdList);

        // 4. result의 type이 type이랑 같은지 확인
        for (ContentReports report : result) {
            Long actualContentId;
            if (type instanceof Posts) {
                actualContentId = report.getPosts().getId();
            } else if (type instanceof Comments) {
                actualContentId = report.getComments().getId();
            } else {
                actualContentId = report.getUser().getId();
            }
            Assertions.assertThat(actualContentId).isEqualTo(expectedContentId);
        }
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
