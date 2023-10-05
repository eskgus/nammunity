package com.eskgus.nammunity.web.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.web.dto.comments.CommentsSaveDto;
import com.eskgus.nammunity.web.dto.comments.CommentsUpdateDto;
import com.eskgus.nammunity.web.posts.PostsApiControllerTest;
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

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Log4j2
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommentsApiControllerTest extends PostsApiControllerTest {
    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() throws Exception {
        signUp();
        savePosts();
    }

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void saveComments() throws Exception {
        // 1. 회원가입 + 게시글 작성 후
        // 2. content, postsId로 CommentsSaveDto 생성
        String content = "comment";
        CommentsSaveDto requestDto = new CommentsSaveDto(content, 1L);

        // 3. "/api/comments"로 commentsSaveDto 담아서 post 요청
        MvcResult mvcResult = mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 4. 응답으로 "OK" 왔는지 확인
        Assertions.assertThat(mvcResult.getResponse().getContentAsString()).contains("OK");

        // 5. db에 저장됐나 확인
        Optional<Comments> result = commentsRepository.findById(1L);
        Assertions.assertThat(result).isPresent();
        Comments comments = result.get();
        Assertions.assertThat(comments.getContent()).isEqualTo(content);
    }

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void updateComments() throws Exception {
        // 1. 회원가입 + 게시글 작성 + 댓글 작성 후
        saveComments();

        // 2. content로 CommentsUpdateDto 생성
        String content = "updated comment";
        CommentsUpdateDto requestDto = new CommentsUpdateDto(content);

        // 3. "/api/comments/1"로 commentsUpdateDto 담아서 put 요청
        MvcResult mvcResult = mockMvc.perform(put("/api/comments/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 4. 응답으로 "OK" 왔는지 확인
        Assertions.assertThat(mvcResult.getResponse().getContentAsString()).contains("OK");

        // 5. db에 저장됐나 확인
        Optional<Comments> result = commentsRepository.findById(1L);
        Assertions.assertThat(result).isPresent();
        Comments comments = result.get();
        Assertions.assertThat(comments.getContent()).isEqualTo(content);
    }

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void deleteComments() throws Exception {
        // 1. 회원가입 + 게시글 작성 + 댓글 작성 후
        saveComments();

        // 2. "/api/comments/1"로 delete 요청
        MvcResult mvcResult = mockMvc.perform(delete("/api/comments/{id}", 1))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 응답으로 "OK" 왔나 확인
        Assertions.assertThat(mvcResult.getResponse().getContentAsString()).contains("OK");

        // 4. db에 1번 댓글 없나 확인
        Assertions.assertThat(commentsRepository.count()).isZero();
    }

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void deleteSelectedComments() throws Exception {
        // 1. 회원가입 + 게시글 작성 + 댓글 작성 * 2 후
        saveComments();
        saveComments();

        // 2. "/api/comments/selected-delete"로 List<Long> commentsId에 1, 2 담아서 delete 요청
        List<Long> commentsId = List.of(1L, 2L);
        MvcResult mvcResult = mockMvc.perform(delete("/api/comments/selected-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(commentsId)))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 응답으로 "OK" 왔는지 확인
        Assertions.assertThat(mvcResult.getResponse().getContentAsString()).contains("OK");

        // 4. db에 저장된 댓글 수 0인지 확인
        Assertions.assertThat(commentsRepository.count()).isZero();
    }
}
