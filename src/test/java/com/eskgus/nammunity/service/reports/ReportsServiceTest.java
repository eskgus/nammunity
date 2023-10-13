package com.eskgus.nammunity.service.reports;

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
import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

@Log4j2
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ReportsServiceTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private ContentReportsRepository contentReportsRepository;

    @Autowired
    private TypesRepository typesRepository;

    @Autowired
    private ReasonsRepository reasonsRepository;

    @Autowired
    private ReportsService reportsService;

    private Long latestUserReportId;
    private Long latestPostReportId;
    private Long latestCommentReportId;

    @BeforeEach
    public void setup() {
        log.info("setup.....");

        // 1. 사용자, 게시글, 댓글 저장
        User user = User.builder()
                .username("username111").password("password111").nickname("nickname1")
                .email("email111@naver.com").role(Role.USER).build();
        userRepository.save(user);
        Assertions.assertThat(userRepository.count()).isOne();

        Posts post = Posts.builder().title("title").content("content").user(user).build();
        postsRepository.save(post);
        Assertions.assertThat(postsRepository.count()).isOne();

        Comments comment = Comments.builder().content("content").posts(post).user(user).build();
        commentsRepository.save(comment);
        Assertions.assertThat(commentsRepository.count()).isOne();

        // 2. 게시글/댓글 신고 10번 + 사용자 신고 3번 저장
        Types postType = typesRepository.findById(1L).get();
        Types commentType = typesRepository.findById(2L).get();
        Types userType = typesRepository.findById(3L).get();

        Long[] reasonIdArr = {1L, 2L, 8L};
        List<Reasons> reasons = new ArrayList<>();
        for (Long id : reasonIdArr) {
            reasons.add(reasonsRepository.findById(id).get());
        }

        // 2-1. 사용자 신고 (신고 사유 8)
        for (int i = 0; i < 3; i++) {
            Reasons reason = reasons.get(i);
            String otherReason = reason.getDetail().equals("기타") ? "기타 사유" : null;
            ContentReports contentReport = ContentReports.builder()
                    .user(user).reporter(user).types(userType).reasons(reason).otherReasons(otherReason).build();
            contentReportsRepository.save(contentReport);
        }
        this.latestUserReportId = contentReportsRepository.count();
        Assertions.assertThat(this.latestUserReportId).isEqualTo(3L);

        // 2-2. 게시글 신고 (신고 사유 1)
        for (int i = 0; i < 10; i++) {
            Reasons reason = reasons.get(i % reasons.size());
            String otherReason = reason.getDetail().equals("기타") ? "기타 사유" : null;
            ContentReports contentReport = ContentReports.builder()
                    .posts(post).reporter(user).types(postType).reasons(reason).otherReasons(otherReason).build();
            contentReportsRepository.save(contentReport);
        }
        this.latestPostReportId = contentReportsRepository.count();
        Assertions.assertThat(this.latestPostReportId).isEqualTo(13L);

        // 2-3. 댓글 신고 (신고 사유 2)
        Collections.rotate(reasons, -1);
        for (int i = 0; i < 10; i++) {
            Reasons reason = reasons.get(i % reasons.size());
            String otherReason = reason.getDetail().equals("기타") ? "기타 사유" : null;
            ContentReports contentReport = ContentReports.builder()
                    .comments(comment).reporter(user).types(commentType).reasons(reason).otherReasons(otherReason).build();
            contentReportsRepository.save(contentReport);
        }
        this.latestCommentReportId = contentReportsRepository.count();
        Assertions.assertThat(this.latestCommentReportId).isEqualTo(23L);
    }

    @Test
    public void findDistinct() {
        User user = userRepository.findById(1L).get();
        Posts post = postsRepository.findById(1L).get();
        Comments comment = commentsRepository.findById(1L).get();

        List<ContentReportDistinctDto> distinctDtos;
        ContentReportDistinctDto distinctDto;

        // 1. endpoint: ""
        distinctDtos = reportsService.findDistinct("");
        Assertions.assertThat(distinctDtos.size()).isGreaterThan(2);

        // 1-1. distinctDtos의 0번째 (사용자 신고)
        distinctDto = distinctDtos.get(0);
        Assertions.assertThat(distinctDto.getTypes().getDetail()).isEqualTo("사용자");
        Assertions.assertThat(distinctDto.getUser().getUsername()).isEqualTo(user.getUsername());

        // 1-2. distinctDtos의 1번째 (게시글 신고)
        distinctDto = distinctDtos.get(1);
        Assertions.assertThat(distinctDto.getTypes().getDetail()).isEqualTo("게시글");
        Assertions.assertThat(distinctDto.getPosts().getTitle()).isEqualTo(post.getTitle());

        // 1-3. distinctDtos의 2번째 (댓글 신고)
        distinctDto = distinctDtos.get(2);
        Assertions.assertThat(distinctDto.getTypes().getDetail()).isEqualTo("댓글");
        Assertions.assertThat(distinctDto.getComments().getContent()).isEqualTo(comment.getContent());

        // 2. endpoint: "posts"
        distinctDtos = reportsService.findDistinct("posts");
        Assertions.assertThat(distinctDtos.size()).isOne();
        distinctDto = distinctDtos.get(0);
        Assertions.assertThat(distinctDto.getTypes().getDetail()).isEqualTo("게시글");
        Assertions.assertThat(distinctDto.getPosts().getTitle()).isEqualTo(post.getTitle());

        // 3. endpoint: "comments"
        distinctDtos = reportsService.findDistinct("comments");
        Assertions.assertThat(distinctDtos.size()).isOne();
        distinctDto = distinctDtos.get(0);
        Assertions.assertThat(distinctDto.getTypes().getDetail()).isEqualTo("댓글");
        Assertions.assertThat(distinctDto.getComments().getContent()).isEqualTo(comment.getContent());

        // 4. endpoint: "users"
        distinctDtos = reportsService.findDistinct("users");
        Assertions.assertThat(distinctDtos.size()).isOne();
        distinctDto = distinctDtos.get(0);
        Assertions.assertThat(distinctDto.getTypes().getDetail()).isEqualTo("사용자");
        Assertions.assertThat(distinctDto.getUser().getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    public void findSummary() {
        User user = userRepository.findById(1L).get();
        Posts post = postsRepository.findById(1L).get();
        Comments comment = commentsRepository.findById(1L).get();

        String latestUserReportedDate = DateTimeUtil.formatDateTime(
                contentReportsRepository.findById(this.latestUserReportId).get().getCreatedDate());
        String latestPostReportedDate = DateTimeUtil.formatDateTime(
                contentReportsRepository.findById(this.latestPostReportId).get().getCreatedDate());
        String latestCommentReportedDate = DateTimeUtil.formatDateTime(
                contentReportsRepository.findById(this.latestCommentReportId).get().getCreatedDate());

        String reason1 = reasonsRepository.findById(1L).get().getDetail();
        String reason2 = reasonsRepository.findById(2L).get().getDetail();
        String otherReason = reasonsRepository.findById(8L).get().getDetail() + ": 기타 사유";

        List<ContentReportSummaryDto> summaryDtos;
        ContentReportSummaryDto summaryDto;

        // 1. endpoint: ""
        summaryDtos = reportsService.findSummary("");
        Assertions.assertThat(summaryDtos.size()).isGreaterThan(2);
        // 1-1. summaryDtos의 0번째 (사용자 신고)
        summaryDto = summaryDtos.get(0);
        Assertions.assertThat(summaryDto.getType()).isEqualTo("사용자");
        Assertions.assertThat(summaryDto.getReporter()).isEqualTo(user.getNickname());
        Assertions.assertThat(summaryDto.getReportedDate()).isEqualTo(latestUserReportedDate);
        Assertions.assertThat(summaryDto.getReason()).isEqualTo(otherReason);
        Assertions.assertThat(summaryDto.getUserId()).isEqualTo(user.getId());
        Assertions.assertThat(summaryDto.getUserExistence()).isTrue();

        // 1-2. summaryDtos의 1번째 (게시글 신고)
        summaryDto = summaryDtos.get(1);
        Assertions.assertThat(summaryDto.getType()).isEqualTo("게시글");
        Assertions.assertThat(summaryDto.getReporter()).isEqualTo(user.getNickname());
        Assertions.assertThat(summaryDto.getReportedDate()).isEqualTo(latestPostReportedDate);
        Assertions.assertThat(summaryDto.getReason()).isEqualTo(reason1);
        Assertions.assertThat(summaryDto.getPostId()).isEqualTo(post.getId());
        Assertions.assertThat(summaryDto.getPostExistence()).isTrue();

        // 1-3. summaryDtos의 2번째 (댓글 신고)
        summaryDto = summaryDtos.get(2);
        Assertions.assertThat(summaryDto.getType()).isEqualTo("댓글");
        Assertions.assertThat(summaryDto.getReporter()).isEqualTo(user.getNickname());
        Assertions.assertThat(summaryDto.getReportedDate()).isEqualTo(latestCommentReportedDate);
        Assertions.assertThat(summaryDto.getReason()).isEqualTo(reason2);
        Assertions.assertThat(summaryDto.getCommentId()).isEqualTo(comment.getId());
        Assertions.assertThat(summaryDto.getCommentExistence()).isTrue();

        // 2. endpoint: "posts"
        summaryDtos = reportsService.findSummary("posts");
        Assertions.assertThat(summaryDtos.size()).isOne();
        summaryDto = summaryDtos.get(0);
        Assertions.assertThat(summaryDto.getType()).isEqualTo("게시글");
        Assertions.assertThat(summaryDto.getReporter()).isEqualTo(user.getNickname());
        Assertions.assertThat(summaryDto.getReportedDate()).isEqualTo(latestPostReportedDate);
        Assertions.assertThat(summaryDto.getReason()).isEqualTo(reason1);
        Assertions.assertThat(summaryDto.getPostId()).isEqualTo(post.getId());
        Assertions.assertThat(summaryDto.getPostExistence()).isTrue();

        // 3. endpoint: "comments"
        summaryDtos = reportsService.findSummary("comments");
        Assertions.assertThat(summaryDtos.size()).isOne();
        summaryDto = summaryDtos.get(0);
        Assertions.assertThat(summaryDto.getType()).isEqualTo("댓글");
        Assertions.assertThat(summaryDto.getReporter()).isEqualTo(user.getNickname());
        Assertions.assertThat(summaryDto.getReportedDate()).isEqualTo(latestCommentReportedDate);
        Assertions.assertThat(summaryDto.getReason()).isEqualTo(reason2);
        Assertions.assertThat(summaryDto.getCommentId()).isEqualTo(comment.getId());
        Assertions.assertThat(summaryDto.getCommentExistence()).isTrue();

        // 4. endpoint: "users"
        summaryDtos = reportsService.findSummary("users");
        Assertions.assertThat(summaryDtos.size()).isOne();
        summaryDto = summaryDtos.get(0);
        Assertions.assertThat(summaryDto.getType()).isEqualTo("사용자");
        Assertions.assertThat(summaryDto.getReporter()).isEqualTo(user.getNickname());
        Assertions.assertThat(summaryDto.getReportedDate()).isEqualTo(latestUserReportedDate);
        Assertions.assertThat(summaryDto.getReason()).isEqualTo(otherReason);
        Assertions.assertThat(summaryDto.getUserId()).isEqualTo(user.getId());
        Assertions.assertThat(summaryDto.getUserExistence()).isTrue();
    }

    @Test
    public void findDetails() {
        // 1. 사용자/게시글/댓글/신고 저장 후
        User user = userRepository.findById(1L).get();
        Posts post = postsRepository.findById(1L).get();
        Comments comment = commentsRepository.findById(1L).get();

        ContentReportDetailDto detailDto;
        List<ContentReportDetailListDto> detailListDtos;

        // 1. type: "post", id = 1L
        detailDto = reportsService.findDetails("post", 1L);
        Assertions.assertThat(detailDto.getPost().getId()).isEqualTo(post.getId());
        Assertions.assertThat(detailDto.getType()).isEqualTo("게시글");
        Assertions.assertThat(detailDto.getPostExistence()).isTrue();
        detailListDtos = detailDto.getReports();
        assertDetailListDtos(detailListDtos, (int) (this.latestPostReportId - this.latestUserReportId),
                this.latestUserReportId);

        // 2. type: "comment", id = 1L
        detailDto = reportsService.findDetails("comment", 1L);
        Assertions.assertThat(detailDto.getComment().getId()).isEqualTo(comment.getId());
        Assertions.assertThat(detailDto.getType()).isEqualTo("댓글");
        Assertions.assertThat(detailDto.getCommentExistence()).isTrue();
        detailListDtos = detailDto.getReports();
        assertDetailListDtos(detailListDtos, (int) (this.latestCommentReportId - this.latestPostReportId),
                this.latestPostReportId);

        // 3. type: "user", id = 1L
        detailDto = reportsService.findDetails("user", 1L);
        Assertions.assertThat(detailDto.getUser().getId()).isEqualTo(user.getId());
        Assertions.assertThat(detailDto.getType()).isEqualTo("사용자");
        Assertions.assertThat(detailDto.getUserExistence()).isTrue();
        detailListDtos = detailDto.getReports();
        assertDetailListDtos(detailListDtos, this.latestUserReportId.intValue(), 0L);
    }

    public void assertDetailListDtos(List<ContentReportDetailListDto> detailListDtos, int iMax, Long id) {
        // ContentReportDetailDto에 들어있는 List<ContentReportDetailListDto>의 size가 i의 최댓값 iMax(마지막 신고의 id)인지 확인
        Assertions.assertThat(detailListDtos.size()).isEqualTo(iMax);

        // 신고 id로 찾은 신고 내역과 List<ContentReportDetailListDto>가 같은지 확인
        for (int i = 0; i < iMax; i++) {
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
