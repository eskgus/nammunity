package com.eskgus.nammunity.web.reports;

import com.eskgus.nammunity.domain.reports.ContentReports;
import com.eskgus.nammunity.domain.reports.ContentReportsRepository;
import com.eskgus.nammunity.domain.reports.ReasonsRepository;
import com.eskgus.nammunity.web.comments.CommentsApiControllerTest;
import com.eskgus.nammunity.web.dto.reports.ContentReportsDeleteDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportsSaveDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReportsApiControllerTest extends CommentsApiControllerTest {
    @Autowired
    private ReasonsRepository reasonsRepository;

    @Autowired
    private ContentReportsRepository contentReportsRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void saveContentReports() throws Exception {
        Long contentsId = 1L;
        Long reasonsId = reasonsRepository.count();
        String otherReasons = "기타 사유";

        // 일반 1. 게시글 신고
        // 1. 회원가입 + 게시글 작성 후
        // 2. postsId, reasonsId, otherReasons로 ContentReportsSaveDto 생성
        ContentReportsSaveDto requestDto = new ContentReportsSaveDto();
        requestDto.setPostsId(contentsId);
        requestDto.setReasonsId(reasonsId);
        requestDto.setOtherReasons(otherReasons);

        // 3. "/api/reports/content"로 contentReportsSaveDto 담아서 post 요청
        MvcResult mvcResult1 = mockMvc.perform(post("/api/reports/content")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 4. 응답으로 "OK" 왔는지 확인
        Assertions.assertThat(mvcResult1.getResponse().getContentAsString()).contains("OK");

        // 5. db에 저장됐나 확인
        Optional<ContentReports> result1 = contentReportsRepository.findById(1L);
        Assertions.assertThat(result1).isPresent();
        ContentReports contentReports1 = result1.get();
        Assertions.assertThat(contentReports1.getPosts().getId()).isEqualTo(contentsId);
        Assertions.assertThat(contentReports1.getReasons().getId()).isEqualTo(reasonsId);
        Assertions.assertThat(contentReports1.getOtherReasons()).isEqualTo(otherReasons);

        // 일반 2. 댓글 신고
        // 1. 회원가입 + 게시글 작성 + 댓글 작성 후
        saveComments();

        // 2. commentsId, reasonsId, otherReasons로 ContentReportsSaveDto 생성
        requestDto.setPostsId(null);
        requestDto.setCommentsId(contentsId);

        // 3. "/api/reports/content"로 contentReportsSaveDto 담아서 post 요청
        MvcResult mvcResult2 = mockMvc.perform(post("/api/reports/content")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 4. 응답으로 "OK" 왔는지 확인
        Assertions.assertThat(mvcResult2.getResponse().getContentAsString()).contains("OK");

        // 5. db에 저장됐나 확인
        Optional<ContentReports> result2 = contentReportsRepository.findById(2L);
        Assertions.assertThat(result2).isPresent();
        ContentReports contentReports2 = result2.get();
        Assertions.assertThat(contentReports2.getComments().getId()).isEqualTo(contentsId);
        Assertions.assertThat(contentReports2.getReasons().getId()).isEqualTo(reasonsId);
        Assertions.assertThat(contentReports2.getOtherReasons()).isEqualTo(otherReasons);

        // 일반 3. 사용자 신고
        // 1. 회원가입 후
        // 2. userId, reasonsId, otherReasons로 ContentReportsSaveDto 생성
        requestDto.setCommentsId(null);
        requestDto.setUserId(contentsId);

        // 3. "/api/reports/content"로 contentReportsSaveDto 담아서 post 요청
        MvcResult mvcResult3 = mockMvc.perform(post("/api/reports/content")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 4. 응답으로 "OK" 왔는지 확인
        Assertions.assertThat(mvcResult3.getResponse().getContentAsString()).contains("OK");

        // 5. db에 저장됐나 확인
        Optional<ContentReports> result3 = contentReportsRepository.findById(3L);
        Assertions.assertThat(result3).isPresent();
        ContentReports contentReports3 = result3.get();
        Assertions.assertThat(contentReports3.getUser().getId()).isEqualTo(contentsId);
        Assertions.assertThat(contentReports3.getReasons().getId()).isEqualTo(reasonsId);
        Assertions.assertThat(contentReports3.getOtherReasons()).isEqualTo(otherReasons);
    }

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void deleteSelectedContentReports() throws Exception {
        // 1. 회원가입 + 게시글 작성 + 댓글 작성 후
        // 2. 게시글/댓글/사용자 신고
        saveContentReports();

        // 3. List<Long> postsId, List<Long> commentsId, List<Long> userId로 ContentReportsDeleteDto 생성
        Long contentsId = 1L;
        List<Long> postsId = List.of(contentsId);
        List<Long> commentsId = List.of(contentsId);
        List<Long> userId = List.of(contentsId);
        ContentReportsDeleteDto requestDto = ContentReportsDeleteDto.builder()
                .postsId(postsId).commentsId(commentsId).userId(userId).build();

        // 4. "/api/reports/content/selected-delete"로 contentReportsDeleteDto 담아서 delete 요청
        MvcResult mvcResult = mockMvc.perform(delete("/api/reports/content/selected-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 5. 응답으로 "OK" 왔는지 확인
        Assertions.assertThat(mvcResult.getResponse().getContentAsString()).contains("OK");

        // 6. db에 저장된 신고 수 0인지 확인
        Assertions.assertThat(contentReportsRepository.count()).isZero();
    }
}
