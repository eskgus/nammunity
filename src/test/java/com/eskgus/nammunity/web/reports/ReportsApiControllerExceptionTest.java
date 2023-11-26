package com.eskgus.nammunity.web.reports;

import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.reports.ContentReportsRepository;
import com.eskgus.nammunity.domain.reports.ReasonsRepository;
import com.eskgus.nammunity.domain.user.BannedUsersRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.reports.ContentReportsDeleteDto;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReportsApiControllerExceptionTest {
    @Autowired
    private TestDB testDB;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReasonsRepository reasonsRepository;

    @Autowired
    private ContentReportsRepository contentReportsRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private BannedUsersRepository bannedUsersRepository;

    @BeforeEach
    public void setUp() {
        this.mockMvc = testDB.setUp();
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    @WithMockUser(username = "username1")
    public void causeExceptionsOnSavingContentReports() throws Exception {
        // 1. user1 회원가입
        testDB.signUp(1L, Role.USER);
        Assertions.assertThat(userRepository.count()).isOne();

        // 예외 1. 신고 사유 선택 x
        requestAndAssertForExceptionOnSavingContentReports("reason", "신고 사유를 선택");

        // 예외 2. 기타 사유 입력 x
        requestAndAssertForExceptionOnSavingContentReports("otherReason", "기타 사유를 입력");

        // 예외 3. 게시글 존재 x
        requestAndAssertForExceptionOnSavingContentReports("post", "게시글이 없");

        // 예외 4. 댓글 존재 x
        requestAndAssertForExceptionOnSavingContentReports("comment", "댓글이 없");

        // 예외 5. 사용자 존재 x
        requestAndAssertForExceptionOnSavingContentReports("user", "존재하지 않는 회원");

        // 예외 6. 신고할 컨텐츠 (신고 분류) 선택 x
        requestAndAssertForExceptionOnSavingContentReports("type", "신고 분류가 선택되지 않");
    }

    @Test
    @WithMockUser(username = "username2", roles = {"ADMIN"})
    public void causeExceptionsOnDeletingSelectedContentReports() throws Exception {
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

        // 4. user2가 게시글 신고 * 10, 댓글 신고 * 10, user1 사용자 신고 * 3
        testDB.savePostReports(postsId, user2);
        testDB.saveCommentReports(commentsId, user2);
        testDB.saveUserReports(user1, user2);

        Long numOfReports = contentReportsRepository.count();
        Assertions.assertThat(numOfReports).isGreaterThan(22);

        // 예외 1. 삭제할 항목 선택 x
        requestAndAssertForExceptionOnDeletingSelectedContentReports(
                "empty", "삭제할 항목을 선택", numOfReports);

        // 예외 2. 게시글 존재 x
        requestAndAssertForExceptionOnDeletingSelectedContentReports(
                "post", "게시글이 없", numOfReports);

        // 예외 3. 댓글 존재 x
        requestAndAssertForExceptionOnDeletingSelectedContentReports(
                "comment", "댓글이 없", numOfReports);

        // 예외 4. 사용자 존재 x
        requestAndAssertForExceptionOnDeletingSelectedContentReports(
                "user", "존재하지 않는 회원", numOfReports);
    }

    @Test
    @WithMockUser(username = "username2", roles = {"ADMIN"})
    public void causeExceptionsOnBanUser() throws Exception {
        // 1. user1 회원가입 + user2 (관리자) 회원가입
        User user1 = userRepository.findById(testDB.signUp(1L, Role.USER)).get();
        User user2 = userRepository.findById(testDB.signUp(2L, Role.ADMIN)).get();
        Assertions.assertThat(userRepository.count()).isGreaterThan(1);

        // 예외 1. 사용자 존재 x
        requestAndAssertForExceptionOnBanUser(3L, "존재하지 않는 회원");

        // 예외 2. 신고 내역 존재 x
        requestAndAssertForExceptionOnBanUser(user1.getId(), "신고 내역이 없");
    }

    private void requestAndAssertForExceptionOnSavingContentReports(String exceptionReason,
                                                                    String responseValue) throws Exception {
        Long contentsId = 2L;
        Long reasonsId = reasonsRepository.count();
        String otherReasons = "기타 사유";

        // 1. ContentReportsSaveDto 생성 + 응답 키 값 설정
        ContentReportsSaveDto requestDto = new ContentReportsSaveDto();
        requestDto.setReasonsId(reasonsId);
        requestDto.setOtherReasons(otherReasons);

        String responseKey = "error";

        if (exceptionReason.equals("reason")) {
            requestDto.setReasonsId(null);
            responseKey = "reasonsId";
        } else if (exceptionReason.equals("otherReason")) {
            requestDto.setOtherReasons(null);
        } else if (exceptionReason.equals("post")) {
            requestDto.setPostsId(contentsId);
        } else if (exceptionReason.equals("comment")) {
            requestDto.setCommentsId(contentsId);
        } else if (exceptionReason.equals("user")) {
            requestDto.setUserId(contentsId);
        }
        // exceptionReason이 "type"일 때는 contentsId 안 넣어서 따로 할 거 x

        // 2. "/api/reports/content"로 contentReportsSaveDto 담아서 post 요청
        MvcResult mvcResult = mockMvc.perform(post("/api/reports/content")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 응답으로 responseKey 왔는지 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey(responseKey);

        // 4. responseKey의 값이 responseValue인지 확인
        Assertions.assertThat((String) map.get(responseKey)).contains(responseValue);

        // 5. db에 저장된 거 없는지 확인
        Assertions.assertThat(contentReportsRepository.count()).isZero();
    }

    private void requestAndAssertForExceptionOnDeletingSelectedContentReports(String exceptionReason,
                                                                              String responseValue,
                                                                              Long numOfReports) throws Exception {
        Long contentsId = 3L;
        List<Long> postsIdList = new ArrayList<>();
        List<Long> commentsIdList = new ArrayList<>();
        List<Long> userIdList = new ArrayList<>();

        // 1. exceptionReason에 따라 -IdList에 id 추가
        if (exceptionReason.equals("post")) {
            postsIdList.add(contentsId);
        } else if (exceptionReason.equals("comment")) {
            commentsIdList.add(contentsId);
        } else if (exceptionReason.equals("user")) {
            userIdList.add(contentsId);
        }

        // 2. -IdList로 ContentReportsDeleteDto 생성
        ContentReportsDeleteDto requestDto = ContentReportsDeleteDto.builder()
                .postsId(postsIdList).commentsId(commentsIdList).userId(userIdList).build();

        // 3. "/api/reports/content/selected-delete"로 contentReportsDeleteDto 담아서 delete 요청
        MvcResult mvcResult = mockMvc.perform(delete("/api/reports/content/selected-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 4. 응답으로 "error" 왔는지 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");

        // 5. "error"의 값이 responseValue인지 확인
        Assertions.assertThat((String) map.get("error")).contains(responseValue);

        // 6. db에 저장된 신고 수 23인지 확인
        Assertions.assertThat(contentReportsRepository.count()).isEqualTo(numOfReports);
    }

    private void requestAndAssertForExceptionOnBanUser(Long userId, String responseValue) throws Exception {
        // 1. user2가 Long userId=3 담아서 "/api/reports/process"로 post 요청
        MvcResult mvcResult = mockMvc.perform(post("/api/reports/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userId)))
                .andExpect(status().isOk())
                .andReturn();

        // 2. 응답으로 "error" 왔나 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");

        // 3. "error"의 값이 responseValue인지 확인
        Assertions.assertThat((String) map.get("error")).contains(responseValue);

        // 4. db에 저장된 거 없는지 확인
        Assertions.assertThat(bannedUsersRepository.count()).isZero();
    }
}
