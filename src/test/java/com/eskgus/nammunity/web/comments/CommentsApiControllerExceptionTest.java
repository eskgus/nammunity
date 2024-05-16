package com.eskgus.nammunity.web.comments;

import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.comments.CommentsSaveDto;
import com.eskgus.nammunity.web.dto.comments.CommentsUpdateDto;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    private User user;
    private Posts post;

    private MockHttpServletRequestBuilder requestBuilder;

    @BeforeEach
    public void setUp() {
        this.mockMvc = testDB.setUp();

        Long user1Id = testDB.signUp(1L, Role.USER);
        this.user = assertOptionalAndGetEntity(userRepository::findById, user1Id);

        Long postId = testDB.savePosts(user);
        this.post = assertOptionalAndGetEntity(postsRepository::findById, postId);
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
    public void saveCommentsExceptions() throws Exception {
        this.requestBuilder = post("/api/comments");

        // 예외 1. 댓글 입력 x
        saveCommentsWithEmptyContent();

        // 예외 2. 1500글자 초과 입력
        saveCommentsWithInvalidContentLength();

        // 예외 3. 게시글 존재 x
        saveCommentsWithNonexistentPostId();
    }

    private void saveCommentsWithEmptyContent() throws Exception {
        CommentsSaveDto requestDto = createCommentsSaveDto("", post.getId());
        ResultMatcher[] resultMatchers = createResultMatchers("댓글을 입력하세요.");

        requestAndAssert(requestDto, resultMatchers);
    }

    private CommentsSaveDto createCommentsSaveDto(String content, Long postId) {
        return new CommentsSaveDto(content, postId);
    }

    private ResultMatcher[] createResultMatchers(String expectedDefaultMessage) {
        ResultMatcher resultMatcher1 = jsonPath("$[0].field").value("content");
        ResultMatcher resultMatcher2 = jsonPath("$[0].defaultMessage").value(expectedDefaultMessage);

        return new ResultMatcher[]{ resultMatcher1, resultMatcher2 };
    }

    private <T> void requestAndAssert(T requestDto, ResultMatcher... resultMatchers) throws Exception {
        mockMvc.perform(requestBuilder
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    for (ResultMatcher resultMatcher : resultMatchers) {
                        resultMatcher.match(result);
                    }
                });
    }

    private void saveCommentsWithInvalidContentLength() throws Exception {
        String content = "c";
        CommentsSaveDto requestDto = createCommentsSaveDto(content.repeat(1501), post.getId());
        ResultMatcher[] resultMatchers = createResultMatchers("댓글은 1500글자 이하여야 합니다.");

        requestAndAssert(requestDto, resultMatchers);
    }

    private void saveCommentsWithNonexistentPostId() throws Exception {
        CommentsSaveDto requestDto = createCommentsSaveDto("comment", post.getId() + 1);
        ResultMatcher resultMatcher = createResultMatcher("해당 게시글이 없습니다.");

        requestAndAssert(requestDto, resultMatcher);
    }

    private ResultMatcher createResultMatcher(String expectedContent) {
        return content().string(expectedContent);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updateCommentsExceptions() throws Exception {
        Long commentId = saveComment();

        // 예외 1. 댓글 입력 x
        updateCommentsWithEmptyContent(commentId);

        // 예외 2. 1500글자 초과 입력
        updateCommentsWithInvalidContentLength(commentId);

        // 예외 3. 댓글 존재 x
        updateCommentsWithNonexistentCommentId(commentId + 1);
    }

    private Long saveComment() {
        Long commentId = testDB.saveComments(post.getId(), user);
        assertOptionalAndGetEntity(commentsRepository::findById, commentId);
        return commentId;
    }

    private void updateCommentsWithEmptyContent(Long commentId) throws Exception {
        this.requestBuilder = put("/api/comments/{id}", commentId);
        CommentsUpdateDto requestDto = createCommentsUpdateDto("");
        ResultMatcher[] resultMatchers = createResultMatchers("댓글을 입력하세요.");

        requestAndAssert(requestDto, resultMatchers);
    }

    private CommentsUpdateDto createCommentsUpdateDto(String content) {
        return new CommentsUpdateDto(content);
    }

    private void updateCommentsWithInvalidContentLength(Long commentId) throws Exception {
        this.requestBuilder = put("/api/comments/{id}", commentId);
        String content = "c";
        CommentsUpdateDto requestDto = createCommentsUpdateDto(content.repeat(1501));
        ResultMatcher[] resultMatchers = createResultMatchers("댓글은 1500글자 이하여야 합니다.");

        requestAndAssert(requestDto, resultMatchers);
    }

    private void updateCommentsWithNonexistentCommentId(Long commentId) throws Exception {
        this.requestBuilder = put("/api/comments/{id}", commentId);
        CommentsUpdateDto requestDto = createCommentsUpdateDto("updated comment");
        ResultMatcher resultMatcher = createResultMatcher("해당 댓글이 없습니다.");

        requestAndAssert(requestDto, resultMatcher);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteCommentsExceptions() throws Exception {
        // 예외 1. 댓글 존재 x
        deleteCommentsWithNonExistentCommentId();
    }

    private void deleteCommentsWithNonExistentCommentId() throws Exception {
        this.requestBuilder = delete("/api/comments/{id}", 1);
        ResultMatcher resultMatcher = createResultMatcher("해당 댓글이 없습니다.");

        requestAndAssert(null, resultMatcher);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteSelectedCommentsExceptions() throws Exception {
        this.requestBuilder = delete("/api/comments/selected-delete");

        // 예외 1. 삭제할 항목 선택 x
        deleteSelectedCommentsWithEmptyCommentIds();

        // 예외 2. 댓글 존재 x
        deleteSelectedCommentsWithNonExistentCommentIds();
    }

    private void deleteSelectedCommentsWithEmptyCommentIds() throws Exception {
        List<Long> requestDto = new ArrayList<>();
        ResultMatcher resultMatcher = createResultMatcher("삭제할 항목을 선택하세요.");

        requestAndAssert(requestDto, resultMatcher);
    }

    private void deleteSelectedCommentsWithNonExistentCommentIds() throws Exception {
        List<Long> requestDto = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Long commentId = saveComment();
            requestDto.add(commentId + 1);
        }
        ResultMatcher resultMatcher = createResultMatcher("해당 댓글이 없습니다.");

        requestAndAssert(requestDto, resultMatcher);
    }
}
