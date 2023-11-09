package com.eskgus.nammunity.web.likes;

import com.eskgus.nammunity.TestDB;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LikesApiControllerTest {
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
    private LikesRepository likesRepository;

    @BeforeEach
    public void setUp() {
        this.mockMvc = testDB.setUp();

        // 1. user1 회원가입
        User user1 = userRepository.findById(testDB.signUp(1L, Role.USER)).get();
        Assertions.assertThat(userRepository.count()).isOne();

        // 2. user1이 게시글 작성
        Long postId = testDB.savePosts(user1);
        Assertions.assertThat(postsRepository.count()).isOne();

        // 3. user1이 댓글 작성
        Long commentId = testDB.saveComments(postId, user1);
        Assertions.assertThat(commentsRepository.count()).isOne();
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    @WithMockUser(username = "username1")
    public void saveLikes() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();

        // 3. user1이 댓글 작성
        Comments comment = commentsRepository.findById(1L).get();

        // 일반 1. 게시글 좋아요
        SavingLikesRequestDto postLikesRequestDto = SavingLikesRequestDto.builder()
                .content("postsId").contentId(post.getId()).expectedLikeId(1L).expectedUserId(user1.getId()).build();
        requestAndAssertToSaveLikes(postLikesRequestDto);

        // 일반 2. 댓글 좋아요
        SavingLikesRequestDto commentLikesRequestDto = SavingLikesRequestDto.builder()
                .content("commentsId").contentId(comment.getId()).expectedLikeId(2L).expectedUserId(user1.getId()).build();
        requestAndAssertToSaveLikes(commentLikesRequestDto);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteLikes() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();

        // 3. user1이 댓글 작성
        Comments comment = commentsRepository.findById(1L).get();

        Long postId = post.getId();
        Long commentId = comment.getId();

        // 4. user1이 게시글 좋아요
        Long postLikeId = testDB.savePostLikes(postId, user1);
        Assertions.assertThat(likesRepository.count()).isOne();

        // 5. user1이 댓글 좋아요
        Long commentLikeId = testDB.saveCommentLikes(commentId, user1);
        Assertions.assertThat(likesRepository.count()).isGreaterThan(1);

        // 일반 1. 게시글 좋아요 취소
        requestAndAssertToDeleteLikes("postsId", postId, postLikeId);

        // 일반 2. 댓글 좋아요 취소
        requestAndAssertToDeleteLikes("commentsId", commentId, commentLikeId);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteSelectedLikes() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();

        // 3. user1이 댓글 작성
        Comments comment = commentsRepository.findById(1L).get();

        Long postId = post.getId();
        Long commentId = comment.getId();

        // 4. user1이 게시글 좋아요
        Long postLikeId = testDB.savePostLikes(postId, user1);
        Assertions.assertThat(likesRepository.count()).isOne();

        // 5. user1이 댓글 좋아요
        Long commentLikeId = testDB.saveCommentLikes(commentId, user1);
        Assertions.assertThat(likesRepository.count()).isGreaterThan(1);

        // 6. "/api/likes/selected-delete"로 List<Long> likesId에 postLikeId, commentLikeId 담아서 delete 요청
        List<Long> likesId = List.of(postLikeId, commentLikeId);

        MvcResult mvcResult = mockMvc.perform(delete("/api/likes/selected-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(likesId)))
                .andExpect(status().isOk())
                .andReturn();

        // 7. 응답으로 "OK" 왔는지 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("OK");

        // 8. db에 저장된 좋아요 수 0인지 확인
        Assertions.assertThat(likesRepository.count()).isZero();
    }

    @Getter
    private static class SavingLikesRequestDto {
        private String content;
        private Long contentId;
        private Long expectedLikeId;
        private Long expectedUserId;

        @Builder
        public SavingLikesRequestDto(String content, Long contentId, Long expectedLikeId, Long expectedUserId) {
            this.content = content;
            this.contentId = contentId;
            this.expectedLikeId = expectedLikeId;
            this.expectedUserId = expectedUserId;
        }
    }

    private void requestAndAssertToSaveLikes(SavingLikesRequestDto likesRequestDto) throws Exception {
        // 1. "/api/likes"로 parameter content=contentId 담아서 post 요청
        MvcResult mvcResult = mockMvc.perform(post("/api/likes")
                        .param(likesRequestDto.getContent(), likesRequestDto.getContentId().toString()))
                .andExpect(status().isOk())
                .andReturn();

        // 2. 응답으로 "OK" 왔는지 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("OK");

        // 3. "OK"의 값이 1/2인지 확인
        Long likeId = Long.valueOf((String) map.get("OK"));
        Assertions.assertThat(likeId).isEqualTo(likesRequestDto.getExpectedLikeId());

        // 4. likeId로 Likes 찾고,
        Optional<Likes> result = likesRepository.findById(likeId);
        Assertions.assertThat(result).isPresent();
        Likes like = result.get();

        // 5. db에 저장됐나 posts/comments, user 확인
        Long actualContentId;
        if (likesRequestDto.getContent().contains("post")) {
            actualContentId = like.getPosts().getId();
        } else {
            actualContentId = like.getComments().getId();
        }
        Assertions.assertThat(actualContentId).isEqualTo(likesRequestDto.getContentId());
        Assertions.assertThat(like.getUser().getId()).isEqualTo(likesRequestDto.getExpectedUserId());
    }

    private void requestAndAssertToDeleteLikes(String content, Long contentId, Long likeId) throws Exception {
        // 1. "/api/likes"로 parameter content=contentId 담아서 delete 요청
        MvcResult mvcResult = mockMvc.perform(delete("/api/likes")
                        .param(content, contentId.toString()))
                .andExpect(status().isOk())
                .andReturn();

        // 2. 응답으로 "OK" 왔는지 확인
        Assertions.assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("OK");

        // 3. likeId로 Likes 찾아서 db에서 지워졌나 확인
        Optional<Likes> result = likesRepository.findById(likeId);
        Assertions.assertThat(result).isNotPresent();
    }
}
