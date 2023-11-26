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
import lombok.Builder;
import lombok.Getter;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommentsApiControllerExceptionTest {
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
    public void causeExceptionsOnSavingComments() throws Exception {
        // 1. user1 회원가입
        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();
        Long postId = post.getId();

        // 예외 1. 댓글 입력 x
        CommentsRequestDto emptyContentRequestDto = CommentsRequestDto.builder()
                .content("").contentId(postId).responseKey("content").responseValue("입력").build();
        requestAndAssertForExceptionOnSavingComments(emptyContentRequestDto);

        // 예외 2. 1500글자 초과 입력
        String content = "c";
        CommentsRequestDto longContentRequestDto = CommentsRequestDto.builder()
                .content(content.repeat(1501)).contentId(postId).responseKey("content").responseValue("1500글자 이하").build();
        requestAndAssertForExceptionOnSavingComments(longContentRequestDto);

        // 예외 3. 게시글 존재 x
        CommentsRequestDto invalidPostRequestDto = CommentsRequestDto.builder()
                .content(content).contentId(postId + 1).responseKey("error").responseValue("게시글이 없").build();
        requestAndAssertForExceptionOnSavingComments(invalidPostRequestDto);
    }

    @Test
    @WithMockUser(username = "username1")
    public void causeExceptionsOnUpdatingComments() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();
        Long postId = post.getId();

        // 3. user1이 댓글 작성
        Long commentId = testDB.saveComments(postId, user1);
        Assertions.assertThat(commentsRepository.count()).isOne();

        // 예외 1. 댓글 입력 x
        CommentsRequestDto emptyContentRequestDto = CommentsRequestDto.builder()
                .content("").contentId(commentId).responseKey("content").responseValue("입력").build();
        requestAndAssertForExceptionOnUpdatingComments(emptyContentRequestDto);

        // 예외 2. 1500글자 초과 입력
        String content = "c";
        CommentsRequestDto longContentRequestDto = CommentsRequestDto.builder()
                .content(content.repeat(1501)).contentId(commentId).responseKey("content").responseValue("1500글자 이하").build();
        requestAndAssertForExceptionOnUpdatingComments(longContentRequestDto);

        // 예외 3. 댓글 존재 x
        CommentsRequestDto invalidPostRequestDto = CommentsRequestDto.builder()
                .content(content).contentId(commentId + 1).responseKey("error").responseValue("댓글이 없").build();
        requestAndAssertForExceptionOnUpdatingComments(invalidPostRequestDto);
    }

    @Test
    @WithMockUser(username = "username1")
    public void causeExceptionsOnDeletingComments() throws Exception {
        // 1. user1 회원가입
        // 2. user1이 게시글 작성
        // 예외 1. 댓글 존재 x
        MvcResult mvcResult = mockMvc.perform(delete("/api/comments/{id}", 1))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("댓글이 없");
        Assertions.assertThat(commentsRepository.count()).isZero();
    }

    @Test
    @WithMockUser(username = "username1")
    public void causeExceptionsOnDeletingSelectedComments() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();
        Long postId = post.getId();

        // 3. user1이 댓글 작성 * 2
        Long commentId1 = testDB.saveComments(postId, user1);
        Long commentId2 = testDB.saveComments(postId, user1);
        long numOfComments = commentsRepository.count();
        Assertions.assertThat(numOfComments).isEqualTo(2);

        // 예외 1. 삭제할 항목 선택 x
        List<Long> commentsId = new ArrayList<>();
        requestAndAssertForExceptionOnDeletingSelectedComments(commentsId, "삭제할 항목을 선택", numOfComments);

        // 예외 2. 댓글 존재 x
        commentsId.add(commentId1);
        commentsId.add(commentId2 + 1);
        requestAndAssertForExceptionOnDeletingSelectedComments(commentsId, "댓글이 없", numOfComments);
    }

    @Getter
    private static class CommentsRequestDto {
        private String content;
        private Long contentId;
        private String responseKey;
        private String responseValue;

        @Builder
        public CommentsRequestDto(String content, Long contentId, String responseKey, String responseValue) {
            this.content = content;
            this.contentId = contentId;
            this.responseKey = responseKey;
            this.responseValue = responseValue;
        }
    }

    private void requestAndAssertForExceptionOnSavingComments(CommentsRequestDto commentsRequestDto) throws Exception {
        // 1. content, contentId(postId)로 CommentsSaveDto 생성
        CommentsSaveDto requestDto = new CommentsSaveDto(commentsRequestDto.getContent(), commentsRequestDto.getContentId());

        // 2. "/api/comments"로 commentsSaveDto 담아서 post 요청
        MvcResult mvcResult = mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 응답으로 responseKey 왔는지 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey(commentsRequestDto.getResponseKey());

        // 4. responseKey의 값이 responseValue인지 확인
        Assertions.assertThat((String) map.get(commentsRequestDto.getResponseKey())).contains(commentsRequestDto.getResponseValue());

        // 5. db에 저장된 댓글 수 0인지 확인
        Assertions.assertThat(commentsRepository.count()).isZero();
    }

    private void requestAndAssertForExceptionOnUpdatingComments(CommentsRequestDto commentsRequestDto) throws Exception {
        // 1. content로 CommentsUpdateDto 생성
        CommentsUpdateDto requestDto = new CommentsUpdateDto(commentsRequestDto.getContent());
        Long commentId = commentsRequestDto.getContentId();

        // 2. "/api/comments/{id}"의 pathVariable=contentId(commentId)로 하고, commentsSaveDto 담아서 put 요청
        MvcResult mvcResult = mockMvc.perform(put("/api/comments/{id}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 응답으로 responseKey 왔는지 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey(commentsRequestDto.getResponseKey());

        // 4. responseKey의 값이 responseValue인지 확인
        Assertions.assertThat((String) map.get(commentsRequestDto.getResponseKey())).contains(commentsRequestDto.getResponseValue());

        // 5. db에 저장된 댓글 내용이 "content"인지 확인
        if (commentId.equals(2L)) {
            commentId -= 1;
        }
        Comments comment = commentsRepository.findById(commentId).get();
        Assertions.assertThat(comment.getContent()).isEqualTo("content");
    }

    private void requestAndAssertForExceptionOnDeletingSelectedComments(List<Long> commentsId, String responseValue,
                                                                        long numOfComments) throws Exception {
        // 1. "/api/comments/selected-delete"로 commentsId 담아서 delete 요청
        MvcResult mvcResult = mockMvc.perform(delete("/api/comments/selected-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(commentsId)))
                .andExpect(status().isOk())
                .andReturn();

        // 2. 응답으로 "error" 왔는지 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");

        // 3. "error"의 값이 responseValue인지 확인
        Assertions.assertThat((String) map.get("error")).contains(responseValue);

        // 4. db에 있는 댓글 수가 numOfComments인지 확인
        Assertions.assertThat(commentsRepository.count()).isEqualTo(numOfComments);
    }
}
