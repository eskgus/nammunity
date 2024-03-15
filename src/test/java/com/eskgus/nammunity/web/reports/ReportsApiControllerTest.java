package com.eskgus.nammunity.web.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.reports.*;
import com.eskgus.nammunity.domain.user.*;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDeleteDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportsSaveDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReportsApiControllerTest {
    @Autowired
    private TestDB testDB;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private ContentReportsRepository contentReportsRepository;

    @Autowired
    private BannedUsersRepository bannedUsersRepository;

    @Autowired
    private ReasonsRepository reasonsRepository;

    @Autowired
    private ContentReportSummaryRepository reportSummaryRepository;

    @BeforeEach
    public void setUp() {
        this.mockMvc = testDB.setUp();
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    @WithMockUser(username = "username2")
    public void saveContentReports() throws Exception {
        // 1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(testDB.signUp(1L, Role.USER)).get();
        User user2 = userRepository.findById(testDB.signUp(2L, Role.USER)).get();
        Assertions.assertThat(userRepository.count()).isGreaterThan(1);

        // 2. user1이 게시글 작성
        Long postsId = testDB.savePosts(user1);
        Assertions.assertThat(postsRepository.count()).isOne();

        // 3. user1이 댓글 작성
        Long commentsId = testDB.saveComments(postsId, user1);
        Assertions.assertThat(commentsRepository.count()).isOne();

        // 일반 1. 게시글 신고
        requestAndAssertToSaveContentReports("post", postsId, 1L);

        // 일반 2. 댓글 신고
        requestAndAssertToSaveContentReports("comment", commentsId, 2L);

        // 일반 3. 사용자 신고
        requestAndAssertToSaveContentReports("user", user1.getId(), 3L);
    }

    @Test
    @WithMockUser(username = "username2", roles = {"ADMIN"})
    public void deleteSelectedContentReports() throws Exception {
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

        // 4. user2가 게시글, 댓글, 사용자 신고 요약 저장
        Posts post = postsRepository.findById(postsId).get();
        Comments comment = commentsRepository.findById(commentsId).get();
        Long postReportSummaryId = testDB.savePostReportSummary(post, user2);
        Long commentReportSummaryId = testDB.saveCommentReportSummary(comment, user2);
        Long userReportSummaryId = testDB.saveUserReportSummary(user1, user2);

        // 5. List<Long> postsIdList, List<Long> commentsIdList, List<Long> userIdList(빈 리스트)로 ContentReportSummaryDeleteDto 생성
        List<Long> postsIdList = List.of(postsId);
        List<Long> commentsIdList = List.of(commentsId);
        List<Long> userIdList = new ArrayList<>();
        ContentReportSummaryDeleteDto requestDto = ContentReportSummaryDeleteDto.builder()
                .postsId(postsIdList).commentsId(commentsIdList).userId(userIdList).build();

        // 6. "/api/reports/content/selected-delete"로 contentReportsDeleteDto 담아서 delete 요청
        MvcResult mvcResult = mockMvc.perform(delete("/api/reports/content/selected-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 7. 응답으로 "OK" 왔는지 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("OK");

        // 8. 게시글/댓글 신고 요약 존재 x, 사용자 신고 요약 존재 o 확인
        Optional<ContentReportSummary> postResult = reportSummaryRepository.findById(postReportSummaryId);
        Assertions.assertThat(postResult).isNotPresent();
        Optional<ContentReportSummary> commentResult = reportSummaryRepository.findById(commentReportSummaryId);
        Assertions.assertThat(commentResult).isNotPresent();
        Optional<ContentReportSummary> userResult = reportSummaryRepository.findById(userReportSummaryId);
        Assertions.assertThat(userResult).isPresent();
    }

    @Test
    @WithMockUser(username = "username2", roles = {"ADMIN"})
    public void banUser() throws Exception {
        // 1. user1 회원가입 + user2 (관리자) 회원가입
        User user1 = userRepository.findById(testDB.signUp(1L, Role.USER)).get();
        User user2 = userRepository.findById(testDB.signUp(2L, Role.ADMIN)).get();
        Assertions.assertThat(userRepository.count()).isGreaterThan(1);

        // 2. user2가 user1 사용자 신고 * 3
        testDB.saveUserReports(user1, user2);
        Assertions.assertThat(contentReportsRepository.count()).isGreaterThan(2);

        // 일반 1. 누적 정지 횟수: 0 (0일 -> 1주)
        Long userId = user1.getId();
        requestAndAssertToBanUser(userId, Period.ofWeeks(1), 1);

        // 일반 2. 누적 정지 횟수: 1 (1주 -> 1개월)
        requestAndAssertToBanUser(userId, Period.ofMonths(1), 2);

        // 일반 3. 누적 정지 횟수: 2 (1개월 -> 1년)
        requestAndAssertToBanUser(userId, Period.ofYears(1), 3);

        // 일반 4. 누적 정지 횟수: 3 (1년 -> 영구)
        requestAndAssertToBanUser(userId, Period.ofYears(100), 4);
    }

    private void requestAndAssertToSaveContentReports(String type, Long contentId, Long expectedReportId) throws Exception {
        Long reasonsId = reasonsRepository.count();
        String otherReasons = "기타 사유";

        // 1. postsId/commentsId/userId, reasonsId, otherReasons로 ContentReportsSaveDto 생성
        ContentReportsSaveDto requestDto = new ContentReportsSaveDto();
        requestDto.setReasonsId(reasonsId);
        requestDto.setOtherReasons(otherReasons);

        if (type.equals("post")) {
            requestDto.setPostsId(contentId);
        } else if (type.equals("comment")) {
            requestDto.setCommentsId(contentId);
        } else {
            requestDto.setUserId(contentId);
        }

        // 2. user2가 "/api/reports/content"로 contentReportsSaveDto 담아서 post 요청
        MvcResult mvcResult = mockMvc.perform(post("/api/reports/content")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 응답으로 "OK" 왔는지 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("OK");

        // 4. "OK"의 값이 1/2/3인지 확인
        Long reportId = Long.valueOf((String) map.get("OK"));
        Assertions.assertThat(reportId).isEqualTo(expectedReportId);

        // 5. reportId로 ContentReports 찾고
        Optional<ContentReports> result = contentReportsRepository.findById(reportId);
        Assertions.assertThat(result).isPresent();
        ContentReports contentReport = result.get();

        // 6. db에 저장됐나 posts/comments/user, reasons, otherReasons 확인
        if (type.equals("post")) {
            Assertions.assertThat(contentReport.getPosts().getId()).isEqualTo(contentId);
        } else if (type.equals("comment")) {
            Assertions.assertThat(contentReport.getComments().getId()).isEqualTo(contentId);
        } else {
            Assertions.assertThat(contentReport.getUser().getId()).isEqualTo(contentId);
        }
        Assertions.assertThat(contentReport.getReasons().getId()).isEqualTo(reasonsId);
        Assertions.assertThat(contentReport.getOtherReasons()).isEqualTo(otherReasons);
    }

    private void requestAndAssertToBanUser(Long userId, Period period, int count) throws Exception {
        // 1. user2가 Long userId=1(user1의 id) 담아서 "/api/reports/process"로 post 요청
        MvcResult mvcResult1 = mockMvc.perform(post("/api/reports/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userId)))
                .andExpect(status().isOk())
                .andReturn();

        // 2. 응답으로 "OK" 왔나 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult1.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("OK");

        // 3. "OK"의 값이 1인지 확인
        Long bannedUserId = Long.valueOf((String) map.get("OK"));
        Assertions.assertThat(bannedUserId).isEqualTo(1);

        // 4. bannedUserId로 BannedUsers 찾고
        Optional<BannedUsers> result = bannedUsersRepository.findById(bannedUserId);
        Assertions.assertThat(result).isPresent();
        BannedUsers bannedUser = result.get();

        // 5. user = user1, period = 1주/1개월/1년/영구, expiredDate = startedDate + period, count = 1/2/3/4인지 확인
        Assertions.assertThat(bannedUser.getUser().getId()).isEqualTo(userId);
        Assertions.assertThat(bannedUser.getPeriod()).isEqualTo(period);
        Assertions.assertThat(bannedUser.getExpiredDate())
                .isEqualTo(bannedUser.getStartedDate().plus(bannedUser.getPeriod()));
        Assertions.assertThat(bannedUser.getCount()).isEqualTo(count);
    }
}
