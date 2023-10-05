package com.eskgus.nammunity.web.reports;

import com.eskgus.nammunity.domain.reports.ContentReportsRepository;
import com.eskgus.nammunity.domain.reports.ReasonsRepository;
import com.eskgus.nammunity.web.dto.reports.ContentReportsDeleteDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportsSaveDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReportsApiControllerExceptionTest extends ReportsApiControllerTest {
    @Autowired
    private ReasonsRepository reasonsRepository;

    @Autowired
    private ContentReportsRepository contentReportsRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .build();

        signUp();
    }

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void causeExceptionsInSavingContentReports() throws Exception {
        Long contentsId = 2L;
        Long reasonsId = reasonsRepository.count();
        String otherReasons = "기타 사유";

        // 예외 1. 신고 사유 선택 x
        ContentReportsSaveDto requestDto = new ContentReportsSaveDto();
        requestDto.setPostsId(contentsId);
        MvcResult mvcResult1 = mockMvc.perform(post("/api/reports/content")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> map = parseResponseJSON(mvcResult1.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("reasonsId");
        Assertions.assertThat((String) map.get("reasonsId")).contains("신고 사유를 선택");

        // 예외 2. 기타 사유 입력 x
        requestDto.setReasonsId(reasonsId);
        MvcResult mvcResult2 = mockMvc.perform(post("/api/reports/content")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();
        map = parseResponseJSON(mvcResult2.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("기타 사유를 입력");

        // 예외 3. 게시글 존재 x
        requestDto.setOtherReasons(otherReasons);
        MvcResult mvcResult3 = mockMvc.perform(post("/api/reports/content")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();
        map = parseResponseJSON(mvcResult3.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("게시글이 없");

        // 예외 4. 댓글 존재 x
        requestDto.setPostsId(null);
        requestDto.setCommentsId(contentsId);
        MvcResult mvcResult4 = mockMvc.perform(post("/api/reports/content")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();
        map = parseResponseJSON(mvcResult4.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("댓글이 없");

        // 예외 5. 사용자 존재 x
        requestDto.setCommentsId(null);
        requestDto.setUserId(contentsId);
        MvcResult mvcResult5 = mockMvc.perform(post("/api/reports/content")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();
        map = parseResponseJSON(mvcResult5.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("존재하지 않는 회원");

        // 예외 6. 신고할 컨텐츠 (신고 분류) 선택 x
        requestDto.setUserId(null);
        MvcResult mvcResult6 = mockMvc.perform(post("/api/reports/content")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();
        map = parseResponseJSON(mvcResult6.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("신고 분류가 선택되지 않");

        Assertions.assertThat(contentReportsRepository.count()).isZero();
    }

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void causeExceptionsInDeletingSelectedContentReports() throws Exception {
        savePosts();
        saveContentReports();

        Long contentsId = 2L;
        List<Long> postsId = new ArrayList<>();
        List<Long> commentsId = new ArrayList<>();
        List<Long> userId = new ArrayList<>();

        // 예외 1. 삭제할 항목 선택 x
        ContentReportsDeleteDto requestDto = ContentReportsDeleteDto.builder()
                .postsId(postsId).commentsId(commentsId).userId(userId).build();
        MvcResult mvcResult1 = mockMvc.perform(delete("/api/reports/content/selected-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> map = parseResponseJSON(mvcResult1.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("삭제할 항목을 선택");

        // 예외 2. 게시글 존재 x
        postsId.add(contentsId);
        MvcResult mvcResult2 = mockMvc.perform(delete("/api/reports/content/selected-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();
        map = parseResponseJSON(mvcResult2.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("게시글이 없");

        // 예외 3. 댓글 존재 x
        postsId.remove(0);
        commentsId.add(contentsId);
        MvcResult mvcResult3 = mockMvc.perform(delete("/api/reports/content/selected-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();
        map = parseResponseJSON(mvcResult3.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("댓글이 없");

        // 예외 4. 사용자 존재 x
        commentsId.remove(0);
        userId.add(contentsId);
        MvcResult mvcResult4 = mockMvc.perform(delete("/api/reports/content/selected-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();
        map = parseResponseJSON(mvcResult4.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("존재하지 않는 회원");

        Assertions.assertThat(contentReportsRepository.count()).isGreaterThan(2L);
    }
}
