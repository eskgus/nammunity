package com.eskgus.nammunity.web.likes;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

    private User user;

    @BeforeEach
    public void setUp() {
        this.mockMvc = testDB.setUp();

        Long userId = testDB.signUp(1L, Role.USER);
        this.user = assertOptionalAndGetEntity(userRepository::findById, userId);
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDB.assertOptionalAndGetEntity(finder, contentId);
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    @WithMockUser(username = "username1")
    public void saveLikesExceptions() throws Exception {
        // 예외 1. 게시글 존재 x
        saveOrDeleteLikesWithNonExistentPostId(post("/api/likes"));

        // 예외 2. 댓글 존재 x
        saveOrDeleteLikesWithNonExistentCommentId(post("/api/likes"));
    }

    private void saveOrDeleteLikesWithNonExistentPostId(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        ResultMatcher resultMatcher = createResultMatcher("해당 게시글이 없습니다.");

        requestAndAssertWithParam(requestBuilder, "postsId", resultMatcher);
    }

    private ResultMatcher createResultMatcher(String expectedContent) {
        return content().string(expectedContent);
    }

    private void requestAndAssertWithParam(MockHttpServletRequestBuilder requestBuilder,
                                           String name, ResultMatcher resultMatcher) throws Exception {
        mockMvc.perform(requestBuilder
                    .param(name, "1"))
                .andExpect(status().isBadRequest())
                .andExpect(resultMatcher);
    }

    private void saveOrDeleteLikesWithNonExistentCommentId(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        ResultMatcher resultMatcher = createResultMatcher("해당 댓글이 없습니다.");

        requestAndAssertWithParam(requestBuilder, "commentsId", resultMatcher);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteLikesExceptions() throws Exception {
        // 예외 1. 게시글 존재 x
        saveOrDeleteLikesWithNonExistentPostId(delete("/api/likes"));

        // 예외 2. 댓글 존재 x
        saveOrDeleteLikesWithNonExistentCommentId(delete("/api/likes"));
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteSelectedLikesExceptions() throws Exception {
        // 예외 1. 삭제할 항목 선택 x
        deleteSelectedLikesWithEmptyLikeIds();

        // 예외 2. 좋아요 존재 x
        deleteSelectedLikesWithNonExistentLikeIds();
    }

    private void deleteSelectedLikesWithEmptyLikeIds() throws Exception {
        List<Long> requestDto = new ArrayList<>();
        ResultMatcher resultMatcher = createResultMatcher("삭제할 항목을 선택하세요.");

        requestAndAssertWithContent(requestDto, resultMatcher);
    }

    private void requestAndAssertWithContent(List<Long> requestDto,
                                             ResultMatcher resultMatcher) throws Exception {
        mockMvc.perform(delete("/api/likes/selected-delete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(resultMatcher);
    }

    private void deleteSelectedLikesWithNonExistentLikeIds() throws Exception {
        List<Long> requestDto = saveLikesAndCreateLikeIds();
        ResultMatcher resultMatcher = createResultMatcher("해당 좋아요가 없습니다.");

        requestAndAssertWithContent(requestDto, resultMatcher);
    }

    private List<Long> saveLikesAndCreateLikeIds() {
        List<Long> requestDto = new ArrayList<>();

        Long postId = savePost();
        Long postLikeId = testDB.savePostLikes(postId, user);
        assertOptionalAndGetEntity(likesRepository::findById, postLikeId);
        requestDto.add(postLikeId);

        Long commentId = saveComment(postId);
        Long commentLikeId = testDB.saveCommentLikes(commentId, user);
        assertOptionalAndGetEntity(likesRepository::findById, commentLikeId);
        requestDto.add(commentLikeId + 1);

        return requestDto;
    }

    private Long savePost() {
        Long postId = testDB.savePosts(user);
        assertOptionalAndGetEntity(postsRepository::findById, postId);
        return postId;
    }

    private Long saveComment(Long postId) {
        Long commentId = testDB.saveComments(postId, user);
        assertOptionalAndGetEntity(commentsRepository::findById, commentId);
        return commentId;
    }
}
