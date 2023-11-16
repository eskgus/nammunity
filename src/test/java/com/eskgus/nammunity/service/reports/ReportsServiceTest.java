package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.TestDB;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.reports.*;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.util.DateTimeUtil;
import com.eskgus.nammunity.web.dto.reports.ContentReportDetailDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportDetailListDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportDistinctDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDto;
import lombok.Builder;
import lombok.Getter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

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
    private ReportsService reportsService;

    private Long latestUserReportId = 3L;
    private Long latestPostReportId = 13L;
    private Long latestCommentReportId = 23L;

    @BeforeEach
    public void setUp() {
        // 1. user1 회원가입 + user2 (관리자) 회원가입
        User user1 = userRepository.findById(testDB.signUp(1L, Role.USER)).get();
        User user2 = userRepository.findById(testDB.signUp(2L, Role.ADMIN)).get();
        Assertions.assertThat(userRepository.count()).isGreaterThan(1);

        // 2. user1이 게시글 작성
        Long postsId = testDB.savePosts(user1);
        Assertions.assertThat(postsRepository.count()).isOne();

        // 3. user1이 댓글 작성
        Long commentsId = testDB.saveComments(postsId, user1);
        Assertions.assertThat(commentsRepository.count()).isOne();

        // 4. user2가 user1 사용자 신고 * 3, 게시글 신고 * 10, 댓글 신고 * 10
        testDB.saveUserReports(user1, user2);
        testDB.savePostReports(postsId, user2);
        testDB.saveCommentReports(commentsId, user2);

        Long numOfReports = contentReportsRepository.count();
        Assertions.assertThat(numOfReports).isGreaterThan(22);
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void findDistinct() {
        // 1. user1 회원가입 + user2 (관리자) 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();

        // 3. user1이 댓글 작성
        Comments comment = commentsRepository.findById(1L).get();

        // 4. user2가 user1 사용자 신고 * 3, 게시글 신고 * 10, 댓글 신고 * 10
        // 5. findDistinct() 호출
        String username = user1.getUsername();
        String title = post.getTitle();
        String content = comment.getContent();

        // 5-1. endpoint = ""
        List<ContentReportDistinctDto> distinctDtos = callAndAssertFindDistinct("", 3);
        // 5-1-1. distinctDtos의 0번째 (사용자 신고)
        assertDistinctDto(distinctDtos.get(0), "사용자", username);
        // 5-1-2. distinctDtos의 1번째 (게시글 신고)
        assertDistinctDto(distinctDtos.get(1), "게시글", title);
        // 5-1-3. distinctDtos의 2번째 (댓글 신고)
        assertDistinctDto(distinctDtos.get(2), "댓글", content);

        // 5-2. endpoint = "posts"
        distinctDtos = callAndAssertFindDistinct("posts", 1);
        assertDistinctDto(distinctDtos.get(0), "게시글", title);

        // 5-3. endpoint = "comments"
        distinctDtos = callAndAssertFindDistinct("comments", 1);
        assertDistinctDto(distinctDtos.get(0), "댓글", content);

        // 5-4. endpoint = "users"
        distinctDtos = callAndAssertFindDistinct("users", 1);
        assertDistinctDto(distinctDtos.get(0), "사용자", username);
    }

    @Test
    public void findSummary() {
        // 1. user1 회원가입 + user2 (관리자) 회원가입
        User user1 = userRepository.findById(1L).get();
        User user2 = userRepository.findById(2L).get();

        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();

        // 3. user1이 댓글 작성
        Comments comment = commentsRepository.findById(1L).get();

        // 4. user2가 user1 사용자 신고 * 3, 게시글 신고 * 10, 댓글 신고 * 10
        // 5. findSummary() 호출
        // expectedReporter
        String reporter = user2.getNickname();

        // expectedReportedDate
        String latestPostReportedDate = DateTimeUtil.formatDateTime(
                contentReportsRepository.findById(this.latestPostReportId).get().getCreatedDate());
        String latestCommentReportedDate = DateTimeUtil.formatDateTime(
                contentReportsRepository.findById(this.latestCommentReportId).get().getCreatedDate());
        String latestUserReportedDate = DateTimeUtil.formatDateTime(
                contentReportsRepository.findById(this.latestUserReportId).get().getCreatedDate());

        // expectedReason
        String reason1 = reasonsRepository.findById(1L).get().getDetail();
        String reason2 = reasonsRepository.findById(2L).get().getDetail();
        String otherReason = reasonsRepository.findById(8L).get().getDetail() + ": 기타 사유";

        // expected values
        SummaryExpectedDto postSummaryExpectedDto = SummaryExpectedDto.builder()
                .expectedType("게시글").expectedReporter(reporter).expectedReportedDate(latestPostReportedDate)
                .expectedReason(reason1).expectedContentId(post.getId()).build();
        SummaryExpectedDto commentSummaryExpectedDto = SummaryExpectedDto.builder()
                .expectedType("댓글").expectedReporter(reporter).expectedReportedDate(latestCommentReportedDate)
                .expectedReason(reason2).expectedContentId(comment.getId()).build();
        SummaryExpectedDto userSummaryExpectedDto = SummaryExpectedDto.builder()
                .expectedType("사용자").expectedReporter(reporter).expectedReportedDate(latestUserReportedDate)
                .expectedReason(otherReason).expectedContentId(user1.getId()).build();

        // 5-1. endpoint: ""
        List<ContentReportSummaryDto> summaryDtos = callAndAssertFindSummary("", 3);
        // 5-1-1. summaryDtos의 0번째 (사용자 신고)
        assertSummaryDto(summaryDtos.get(0), userSummaryExpectedDto);
        // 5-1-2. summaryDtos의 1번째 (게시글 신고)
        assertSummaryDto(summaryDtos.get(1), postSummaryExpectedDto);
        // 5-1-3. summaryDtos의 2번째 (댓글 신고)
        assertSummaryDto(summaryDtos.get(2), commentSummaryExpectedDto);

        // 5-2. endpoint: "posts"
        summaryDtos = callAndAssertFindSummary("posts", 1);
        assertSummaryDto(summaryDtos.get(0), postSummaryExpectedDto);

        // 5-3. endpoint = "comments"
        summaryDtos = callAndAssertFindSummary("comments", 1);
        assertSummaryDto(summaryDtos.get(0), commentSummaryExpectedDto);

        // 5-4. endpoint = "users"
        summaryDtos = callAndAssertFindSummary("users", 1);
        assertSummaryDto(summaryDtos.get(0), userSummaryExpectedDto);
    }

    @Test
    public void findDetails() {
        // 1. user1 회원가입 + user2 (관리자) 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();

        // 3. user1이 댓글 작성
        Comments comment = commentsRepository.findById(1L).get();

        // 4. user2가 user1 사용자 신고 * 3, 게시글 신고 * 10, 댓글 신고 * 10
        int numOfPostReports = (int) (this.latestPostReportId - this.latestUserReportId);
        int numOfCommentReports = (int) (this.latestCommentReportId - this.latestPostReportId);
        int numOfUserReports = this.latestUserReportId.intValue();

        // 5. findDetails() 호출
        // 5-1. type: "post", id = post의 id
        List<ContentReportDetailListDto> detailListDtos =
                callAndAssertFindDetails("post", post.getId(), numOfPostReports);
        assertDetailListDtos(detailListDtos, numOfPostReports, this.latestUserReportId);

        // 5-2. type: "comment", id = comment의 id
        detailListDtos = callAndAssertFindDetails("comment", comment.getId(), numOfCommentReports);
        assertDetailListDtos(detailListDtos, numOfCommentReports, this.latestPostReportId);

        // 5-3. type: "user", id = user1의 id
        detailListDtos = callAndAssertFindDetails("user", user1.getId(), numOfUserReports);
        assertDetailListDtos(detailListDtos, numOfUserReports, 0L);
    }

    @Getter
    private static class SummaryExpectedDto {
        private String expectedType;
        private String expectedReporter;
        private String expectedReportedDate;
        private String expectedReason;
        private Long expectedContentId;

        @Builder
        public SummaryExpectedDto(String expectedType, String expectedReporter, String expectedReportedDate,
                                  String expectedReason, Long expectedContentId) {
            this.expectedType = expectedType;
            this.expectedReporter = expectedReporter;
            this.expectedReportedDate = expectedReportedDate;
            this.expectedReason = expectedReason;
            this.expectedContentId = expectedContentId;
        }
    }

    private List<ContentReportDistinctDto> callAndAssertFindDistinct(String endpoint, int expectedSize) {
        // findDistinct() 호출하고 리턴 값 List<ContentReportDistinctDto>의 size가 expectedSize랑 같은지 확인
        List<ContentReportDistinctDto> distinctDtos = reportsService.findDistinct(endpoint);
        Assertions.assertThat(distinctDtos.size()).isEqualTo(expectedSize);
        return distinctDtos;
    }

    private void assertDistinctDto(ContentReportDistinctDto distinctDto, String expectedType, String expectedContent) {
        // distinctDto의 type, content(post: title, comment: content, user: username) 확인
        Assertions.assertThat(distinctDto.getTypes().getDetail()).isEqualTo(expectedType);

        String actualContent;
        if ("게시글".equals(expectedType)) {
            actualContent = distinctDto.getPosts().getTitle();
        } else if ("댓글".equals(expectedType)) {
            actualContent = distinctDto.getComments().getContent();
        } else {
            actualContent = distinctDto.getUser().getUsername();
        }
        Assertions.assertThat(actualContent).isEqualTo(expectedContent);
    }

    private List<ContentReportSummaryDto> callAndAssertFindSummary(String endpoint, int expectedSize) {
        // findSummary() 호출하고 리턴 값 List<ContentReportSummaryDto>의 size가 expectedSize랑 같은지 확인
        List<ContentReportSummaryDto> summaryDtos = reportsService.findSummary(endpoint);
        Assertions.assertThat(summaryDtos.size()).isEqualTo(expectedSize);
        return summaryDtos;
    }

    private void assertSummaryDto(ContentReportSummaryDto summaryDto, SummaryExpectedDto summaryExpectedDto) {
        // summaryDto의 type, reporter, reportedDate, reason, content(post, comment, user) id/existence 확인
        Assertions.assertThat(summaryDto.getType()).isEqualTo(summaryExpectedDto.getExpectedType());
        Assertions.assertThat(summaryDto.getReporter()).isEqualTo(summaryExpectedDto.getExpectedReporter());
        Assertions.assertThat(summaryDto.getReportedDate()).isEqualTo(summaryExpectedDto.getExpectedReportedDate());
        Assertions.assertThat(summaryDto.getReason()).isEqualTo(summaryExpectedDto.getExpectedReason());

        Long actualContentId;
        boolean actualContentExistence;
        if ("게시글".equals(summaryExpectedDto.getExpectedType())) {
            actualContentId = summaryDto.getPostId();
            actualContentExistence = summaryDto.getPostExistence();
        } else if ("댓글".equals(summaryExpectedDto.getExpectedType())) {
            actualContentId = summaryDto.getCommentId();
            actualContentExistence = summaryDto.getCommentExistence();
        } else {
            actualContentId = summaryDto.getUserId();
            actualContentExistence = summaryDto.getUserExistence();
        }
        Assertions.assertThat(actualContentId).isEqualTo(summaryExpectedDto.getExpectedContentId());
        Assertions.assertThat(actualContentExistence).isTrue();
    }

    private List<ContentReportDetailListDto> callAndAssertFindDetails(String type, Long expectedContentId,
                                                                      int expectedNumOfReports) {
        // findDetails() 호출하고 리턴 값 ContentReportDetailListDto의 content(post, comment, user) id/existence, type 확인
        ContentReportDetailDto detailDto = reportsService.findDetails(type, expectedContentId);

        Long actualContentId;
        String expectedType;
        boolean actualContentExistence;
        if ("post".equals(type)) {
            actualContentId = detailDto.getPost().getId();
            expectedType = "게시글";
            actualContentExistence = detailDto.getPostExistence();
        } else if ("comment".equals(type)) {
            actualContentId = detailDto.getComment().getId();
            expectedType = "댓글";
            actualContentExistence = detailDto.getCommentExistence();
        } else {
            actualContentId = detailDto.getUser().getId();
            expectedType = "사용자";
            actualContentExistence = detailDto.getUserExistence();
        }
        Assertions.assertThat(actualContentId).isEqualTo(expectedContentId);
        Assertions.assertThat(detailDto.getType()).isEqualTo(expectedType);
        Assertions.assertThat(actualContentExistence).isTrue();
        Assertions.assertThat(detailDto.getNumOfReports()).isEqualTo(expectedNumOfReports);

        // ContentReportDetailDto에 들어있는 List<ContentReportDetailListDto> 검증하기 위해 리턴
        return detailDto.getReports();
    }

    private void assertDetailListDtos(List<ContentReportDetailListDto> detailListDtos, int numOfReports, Long id) {
        // ContentReportDetailDto에 들어있는 List<ContentReportDetailListDto>의 size가 numOfReports(신고 세부 내역 개수)인지 확인
        Assertions.assertThat(detailListDtos.size()).isEqualTo(numOfReports);

        // 신고 id로 찾은 신고 내역과 List<ContentReportDetailListDto>가 같은지 확인
        for (int i = 0; i < numOfReports; i++) {
            ContentReports report = contentReportsRepository.findById(i + 1 + id).get();
            ContentReportDetailListDto detailListDto = detailListDtos.get(i);
            String reason = report.getReasons().getDetail();
            if (reason.equals("기타")) {
                reason += ": " + report.getOtherReasons();
            }

            Assertions.assertThat(detailListDto.getId()).isEqualTo(report.getId());
            Assertions.assertThat(detailListDto.getReporter()).isEqualTo(report.getReporter().getNickname());
            Assertions.assertThat(detailListDto.getReportedDate())
                    .isEqualTo(DateTimeUtil.formatDateTime(report.getCreatedDate()));
            Assertions.assertThat(detailListDto.getReason()).isEqualTo(reason);
        }
    }
}
