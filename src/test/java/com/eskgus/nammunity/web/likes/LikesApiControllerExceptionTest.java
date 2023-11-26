package com.eskgus.nammunity.web.likes;

import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
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
public class LikesApiControllerExceptionTest {
    @Autowired
    private TestDB testDB;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LikesRepository likesRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @BeforeEach
    public void setUp() {
        this.mockMvc = testDB.setUp();

        // 1. user1 회원가입
        testDB.signUp(1L, Role.USER);
        Assertions.assertThat(userRepository.count()).isOne();
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    @WithMockUser(username = "username1")
    public void causeExceptionsOnSavingLikes() throws Exception {
        // 1. user1 회원가입
        // 예외 1. 게시글 존재 x
        requestAndAssertForExceptionOnSavingLikes("postsId", "1", "게시글이 없");

        // 예외 2. 댓글 존재 x
        requestAndAssertForExceptionOnSavingLikes("commentsId", "1", "댓글이 없");
    }

    @Test
    @WithMockUser(username = "username1")
    public void causeExceptionsOnDeletingLikes() throws Exception {
        // 1. user1 회원가입
        // 예외 1. 게시글 존재 x
        requestAndAssertForExceptionOnDeletingLikes("postsId", "1", "게시글이 없");

        // 예외 2. 댓글 존재 x
        requestAndAssertForExceptionOnDeletingLikes("commentsId", "1", "댓글이 없");
    }

    @Test
    @WithMockUser(username = "username1")
    public void causeExceptionsOnDeletingSelectedLikes() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성
        Long postId = testDB.savePosts(user1);
        Assertions.assertThat(postsRepository.count()).isOne();

        // 3. user1이 댓글 작성
        Long commentId = testDB.saveComments(postId, user1);
        Assertions.assertThat(commentsRepository.count()).isOne();

        // 4. user1이 게시글 좋아요
        testDB.savePostLikes(postId, user1);
        Assertions.assertThat(likesRepository.count()).isOne();

        // 5. user1이 댓글 좋아요
        testDB.saveCommentLikes(commentId, user1);
        Assertions.assertThat(likesRepository.count()).isGreaterThan(1);

        // 예외 1. 삭제할 항목 선택 x
        // 1-1. List<Long> likesId 생성
        List<Long> likesId = new ArrayList<>();

        // 1-2. "/api/likes/selected-delete"로 likesId 담아서 delete 요청
        MvcResult mvcResult = mockMvc.perform(delete("/api/likes/selected-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(likesId)))
                .andExpect(status().isOk())
                .andReturn();

        // 1-3. 응답으로 "error" 왔는지 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");

        // 1-4. "error"의 값이 "삭제할 항목을 선택"인지 확인
        Assertions.assertThat((String) map.get("error")).contains("삭제할 항목을 선택");

        // 1-5. db에 저장된 좋아요 수 2인지 확인
        Assertions.assertThat(likesRepository.count()).isEqualTo(2);
    }

    private void requestAndAssertForExceptionOnSavingLikes(String content, String contentId, String responseValue) throws Exception {
        // 1. "/api/likes"로 parameter content=contentId 담아서 post 요청
        MvcResult mvcResult = mockMvc.perform(post("/api/likes")
                        .param(content, contentId))
                .andExpect(status().isOk())
                .andReturn();

        // 2. 응답으로 "error" 왔는지 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");

        // 3. "error"의 값이 responseValue인지 확인
        Assertions.assertThat((String) map.get("error")).contains(responseValue);

        // 4. db에 저장된 거 없는지 확인
        Assertions.assertThat(likesRepository.count()).isZero();
    }

    private void requestAndAssertForExceptionOnDeletingLikes(String content, String contentId, String responseValue) throws Exception {
        // 1. "/api/likes"로 parameter content=contentId 담아서 delete 요청
        MvcResult mvcResult = mockMvc.perform(delete("/api/likes")
                        .param(content, contentId))
                .andExpect(status().isOk())
                .andReturn();

        // 2. 응답으로 responseValue 왔는지 확인
        Assertions.assertThat(mvcResult.getResponse().getContentAsString()).contains(responseValue);

        // 3. db에 저장된 거 없는지 확인 (posts/comments 지우면 likes도 같이 삭제돼서 posts/comments 없어서 예외 발생하면 likes도 없음)
        Assertions.assertThat(likesRepository.count()).isZero();
    }
}
