package com.eskgus.nammunity.web.comments;

import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.comments.CommentsSaveDto;
import com.eskgus.nammunity.web.dto.comments.CommentsUpdateDto;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommentsApiControllerTest {
    @Autowired
    private TestDB testDB;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @BeforeEach
    public void setUp() {
        this.mockMvc = testDB.setUp();

        // 1. user1 회원가입
        User user1 = userRepository.findById(testDB.signUp(1L, Role.USER)).get();
        Assertions.assertThat(userRepository.count()).isOne();

        // 2. user1이 게시글 작성
        testDB.savePosts(user1);
        Assertions.assertThat(postsRepository.count()).isOne();
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    @WithMockUser(username = "username1")
    public void saveComments() throws Exception {
        // 1. user1 회원가입
        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();

        // 3. content, postId로 CommentsSaveDto 생성
        Long postId = post.getId();
        String content = "content";
        CommentsSaveDto requestDto = new CommentsSaveDto(content, postId);

        // 4. "/api/comments"로 commentsSaveDto 담아서 post 요청
        MvcResult mvcResult = mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 5. 응답 확인
        assertMvcResult(mvcResult, true, content);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updateComments() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();
        Long postId = post.getId();

        // 3. user1이 댓글 작성
        Long commentId = testDB.saveComments(postId, user1);
        Assertions.assertThat(commentsRepository.count()).isOne();

        // 4. content로 CommentsUpdateDto 생성
        String content = "updated content";
        CommentsUpdateDto requestDto = new CommentsUpdateDto(content);

        // 5. "/api/comments/{id}"의 pathVariable=commentId로 하고, commentsUpdateDto 담아서 put 요청
        MvcResult mvcResult = mockMvc.perform(put("/api/comments/{id}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 6. 응답 확인
        assertMvcResult(mvcResult, true, content);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteComments() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();
        Long postId = post.getId();

        // 3. user1이 댓글 작성
        Long commentId = testDB.saveComments(postId, user1);
        Assertions.assertThat(commentsRepository.count()).isOne();

        // 4. "/api/comments/{id}"의 pathVariable=commentId로 하고, delete 요청
        MvcResult mvcResult = mockMvc.perform(delete("/api/comments/{id}", commentId))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 응답 확인
        assertMvcResult(mvcResult, false, null);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteSelectedComments() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();
        Long postId = post.getId();

        // 3. user1이 댓글 작성
        Long commentId1 = testDB.saveComments(postId, user1);
        Long commentId2 = testDB.saveComments(postId, user1);
        Assertions.assertThat(commentsRepository.count()).isEqualTo(2);

        // 4. "/api/comments/selected-delete"로 List<Long> commentsId에 commentId1/2 담아서 delete 요청
        List<Long> commentsId = List.of(commentId1, commentId2);
        MvcResult mvcResult = mockMvc.perform(delete("/api/comments/selected-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(commentsId)))
                .andExpect(status().isOk())
                .andReturn();

        // 5. 응답 확인
        assertMvcResult(mvcResult, false, null);
    }

    private void assertMvcResult(MvcResult mvcResult, boolean expectedResult, String content) throws Exception {
        // 1. 응답으로 "OK" 왔는지 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("OK");

        // 2. expectedResult가 false면 댓글 삭제한 거 => db에 있는 댓글 수 0인지 확인하고 리턴
        if (!expectedResult) {
            Assertions.assertThat(commentsRepository.count()).isZero();
            return;
        }

        // 3. expectedResult가 true면 댓글 삭제 말고 저장 또는 수정한 거
        // 3-1. "OK"의 값으로 comments 찾고
        Long commentId = Long.valueOf((String) map.get("OK"));
        Optional<Comments> result = commentsRepository.findById(commentId);
        Assertions.assertThat(result).isPresent();

        // 3-2. db에 저장됐나 확인
        Comments comment = result.get();
        Assertions.assertThat(comment.getContent()).isEqualTo(content);
    }
}
