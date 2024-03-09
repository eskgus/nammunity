package com.eskgus.nammunity.service.reports;

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
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.reports.*;
import com.eskgus.nammunity.web.dto.user.UsersListDto;
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
        assertThat(contentReportSummaryRepository.count()).isEqualTo(3);
    }

    private <T> void callAndAssertSaveContentReports(T contents, User reporter) {
        ContentReportsSaveDto requestDto = createReportsSaveDto(contents);
        Long reportId = reportsService.saveContentReports(requestDto, reporter.getUsername());

        Optional<ContentReports> result = contentReportsRepository.findById(reportId);
        assertThat(result).isPresent();

        ContentReports report = result.get();
        assertThat(report.getReporter().getId()).isEqualTo(reporter.getId());
        assertContentId(report, contents);
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

    private <T> void assertContentId(ContentReports report, T contents) {
        Long actualId = getActualId(report);
        Long expectedId = getExpectedId(contents);
        assertThat(actualId).isEqualTo(expectedId);
    }

    private Long getActualId(ContentReports report) {
        String type = report.getTypes().getDetail();
        if (type.equals("게시글")) {
            return report.getPosts().getId();
        } else if (type.equals("댓글")) {
            return report.getComments().getId();
        }
        return report.getUser().getId();
    }

    private <T> Long getExpectedId(T contents) {
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

    // TODO: reportsService.createContentListDto 테스트
    @Test
    public void createContentListDto() {
        // this.post로 호출
        PostsListDto postsListDto = reportsService.createContentListDto(post);
        // PostsListDto 검증
        assertThat(postsListDto.getId()).isEqualTo(post.getId());
        assertThat(postsListDto.getAuthor()).isEqualTo(post.getUser().getNickname());

        // this.comment로 호출
        CommentsListDto commentsListDto = reportsService.createContentListDto(comment);
        // CommentsListDto 검증
        assertThat(commentsListDto.getCommentsId()).isEqualTo(comment.getId());
        assertThat(commentsListDto.getPostsId()).isEqualTo(comment.getPosts().getId());
        assertThat(commentsListDto.getAuthor()).isEqualTo(comment.getUser().getId());

        // this.user1로 호출
        UsersListDto usersListDto = reportsService.createContentListDto(user1);
        // UsersListDto 검증
        assertThat(usersListDto.getId()).isEqualTo(user1.getId());
    }

    // TODO: reportsService.findDetails 테스트 (findDetails() 수정 후)
    @Test
    public void findDetails() {
//        // 1. user1 회원가입 + user2 (관리자) 회원가입
//        User user1 = userRepository.findById(1L).get();
//
//        // 2. user1이 게시글 작성
//        Posts post = postsRepository.findById(1L).get();
//
//        // 3. user1이 댓글 작성
//        Comments comment = commentsRepository.findById(1L).get();
//
//        // 4. user2가 user1 사용자 신고 * 3, 게시글 신고 * 10, 댓글 신고 * 10
//        int numOfPostReports = (int) (this.latestPostReportId - this.latestUserReportId);
//        int numOfCommentReports = (int) (this.latestCommentReportId - this.latestPostReportId);
//        int numOfUserReports = this.latestUserReportId.intValue();
//
//        // 5. findDetails() 호출
//        // 5-1. type: "post", id = post의 id
//        List<ContentReportDetailListDto> detailListDtos =
//                callAndAssertFindDetails("post", post.getId(), numOfPostReports);
//        assertDetailListDtos(detailListDtos, numOfPostReports, this.latestUserReportId);
//
//        // 5-2. type: "comment", id = comment의 id
//        detailListDtos = callAndAssertFindDetails("comment", comment.getId(), numOfCommentReports);
//        assertDetailListDtos(detailListDtos, numOfCommentReports, this.latestPostReportId);
//
//        // 5-3. type: "user", id = user1의 id
//        detailListDtos = callAndAssertFindDetails("user", user1.getId(), numOfUserReports);
//        assertDetailListDtos(detailListDtos, numOfUserReports, 0L);
    }

//    private List<ContentReportDetailListDto> callAndAssertFindDetails(String type, Long expectedContentId,
//                                                                      int expectedNumOfReports) {
//        // findDetails() 호출하고 리턴 값 ContentReportDetailListDto의 content(post, comment, user) id/existence, type 확인
//        ContentReportDetailDto detailDto = reportsService.findDetails(type, expectedContentId);
//
//        Long actualContentId;
//        String expectedType;
//        boolean actualContentExistence;
//        if ("post".equals(type)) {
//            actualContentId = detailDto.getPost().getId();
//            expectedType = "게시글";
//            actualContentExistence = detailDto.getPostExistence();
//        } else if ("comment".equals(type)) {
//            actualContentId = detailDto.getComment().getId();
//            expectedType = "댓글";
//            actualContentExistence = detailDto.getCommentExistence();
//        } else {
//            actualContentId = detailDto.getUser().getId();
//            expectedType = "사용자";
//            actualContentExistence = detailDto.getUserExistence();
//        }
//        Assertions.assertThat(actualContentId).isEqualTo(expectedContentId);
//        Assertions.assertThat(detailDto.getType()).isEqualTo(expectedType);
//        Assertions.assertThat(actualContentExistence).isTrue();
//        Assertions.assertThat(detailDto.getNumOfReports()).isEqualTo(expectedNumOfReports);
//
//        // ContentReportDetailDto에 들어있는 List<ContentReportDetailListDto> 검증하기 위해 리턴
//        return detailDto.getReports();
//    }
}
