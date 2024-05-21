package com.eskgus.nammunity.web.likes;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LikesApiControllerExceptionTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

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
        ResultMatcher resultMatcher = mockMvcTestHelper.createResultMatcher("해당 게시글이 없습니다.");

        mockMvcTestHelper.requestAndAssertStatusIsBadRequestWithParam(requestBuilder, "postsId", 1L, resultMatcher);
    }

    private void saveOrDeleteLikesWithNonExistentCommentId(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        ResultMatcher resultMatcher = mockMvcTestHelper.createResultMatcher("해당 댓글이 없습니다.");

        mockMvcTestHelper.requestAndAssertStatusIsBadRequestWithParam(requestBuilder, "commentsId", 1L, resultMatcher);
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
        MockHttpServletRequestBuilder requestBuilder = delete("/api/likes/selected-delete");

        // 예외 1. 삭제할 항목 선택 x
        deleteSelectedLikesWithEmptyLikeIds(requestBuilder);

        // 예외 2. 좋아요 존재 x
        deleteSelectedLikesWithNonExistentLikeIds(requestBuilder);
    }

    private void deleteSelectedLikesWithEmptyLikeIds(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        List<Long> requestDto = new ArrayList<>();
        ResultMatcher resultMatcher = mockMvcTestHelper.createResultMatcher("삭제할 항목을 선택하세요.");

        mockMvcTestHelper.requestAndAssertStatusIsBadRequest(requestBuilder, requestDto, resultMatcher);
    }

    private void deleteSelectedLikesWithNonExistentLikeIds(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        List<Long> requestDto = saveLikesAndCreateLikeIds();
        ResultMatcher resultMatcher = mockMvcTestHelper.createResultMatcher("해당 좋아요가 없습니다.");

        mockMvcTestHelper.requestAndAssertStatusIsBadRequest(requestBuilder, requestDto, resultMatcher);
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
