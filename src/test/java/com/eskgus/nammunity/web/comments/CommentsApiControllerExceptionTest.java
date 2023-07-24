package com.eskgus.nammunity.web.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.web.dto.comments.CommentsSaveDto;
import com.eskgus.nammunity.web.dto.comments.CommentsUpdateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
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

import java.util.Map;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Log4j2
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommentsApiControllerExceptionTest extends CommentsApiControllerTest {
    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

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
        savePosts();
    }

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void causeExceptionsInSavingComments() throws Exception {
        // 예외 1. 댓글 입력 x
        CommentsSaveDto requestDto1 = new CommentsSaveDto("", 1L);
        MvcResult mvcResult1 = mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto1)))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> map = parseResponseJSON(mvcResult1.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("content");
        Assertions.assertThat((String) map.get("content")).contains("입력");

        // 예외 2. 1500글자 초과 입력
        String content = "c";
        CommentsSaveDto requestDto2 = new CommentsSaveDto(content.repeat(1501), 1L);
        MvcResult mvcResult2 = mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto2)))
                .andExpect(status().isOk())
                .andReturn();
        map = parseResponseJSON(mvcResult2.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("content");
        Assertions.assertThat((String) map.get("content")).contains("1500글자 이하");

        // 예외 3. 게시글 존재 x
        CommentsSaveDto requestDto3 = new CommentsSaveDto(content, 2L);
        MvcResult mvcResult3 = mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto3)))
                .andExpect(status().isOk())
                .andReturn();
        map = parseResponseJSON(mvcResult3.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("게시글이 없");

        Assertions.assertThat(postsRepository.existsById(2L)).isFalse();
        Assertions.assertThat(commentsRepository.count()).isZero();
    }

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void causeExceptionsInUpdatingComments() throws Exception {
        saveComments();

        // 예외 1. 댓글 입력 x
        CommentsUpdateDto requestDto1 = new CommentsUpdateDto("");
        MvcResult mvcResult1 = mockMvc.perform(put("/api/comments/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto1)))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> map = parseResponseJSON(mvcResult1.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("content");
        Assertions.assertThat((String) map.get("content")).contains("입력");

        // 예외 2. 1500글자 초과 입력
        String content = "c";
        CommentsUpdateDto requestDto2 = new CommentsUpdateDto(content.repeat(1501));
        MvcResult mvcResult2 = mockMvc.perform(put("/api/comments/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto2)))
                .andExpect(status().isOk())
                .andReturn();
        map = parseResponseJSON(mvcResult2.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("content");
        Assertions.assertThat((String) map.get("content")).contains("1500글자 이하");

        // 예외 3. 게시글 존재 x
        CommentsUpdateDto requestDto3 = new CommentsUpdateDto(content);
        MvcResult mvcResult3 = mockMvc.perform(put("/api/comments/{id}", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto3)))
                .andExpect(status().isOk())
                .andReturn();
        map = parseResponseJSON(mvcResult3.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("댓글이 없");

        Assertions.assertThat(commentsRepository.existsById(2L)).isFalse();
        Comments comments = commentsRepository.findById(1L).get();
        Assertions.assertThat(comments.getContent()).isEqualTo("comment");
    }

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void causeExceptionsInDeletingComments() throws Exception {
        // 예외 1. 댓글 존재 x
        MvcResult mvcResult = mockMvc.perform(delete("/api/comments/{id}", 1))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> map = parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("댓글이 없");
        Assertions.assertThat(commentsRepository.count()).isZero();
    }
}
