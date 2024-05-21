package com.eskgus.nammunity.web.comments;

import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.comments.CommentsSaveDto;
import com.eskgus.nammunity.web.dto.comments.CommentsUpdateDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommentsApiControllerTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    private User user;
    private Posts post;

    @BeforeEach
    public void setUp() {
        Long userId = testDB.signUp(1L, Role.USER);
        this.user = assertOptionalAndGetEntity(userRepository::findById, userId);

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
    public void saveComments() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post("/api/comments");
        CommentsSaveDto requestDto = createCommentsSaveDto();

        mockMvcTestHelper.requestAndAssertStatusIsOk(requestBuilder, requestDto);
    }

    private CommentsSaveDto createCommentsSaveDto() {
        String content = "comment";
        Long postId = post.getId();
        return new CommentsSaveDto(content, postId);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updateComments() throws Exception {
        Long commentId = saveComment();

        MockHttpServletRequestBuilder requestBuilder = put("/api/comments/{id}", commentId);
        CommentsUpdateDto requestDto = createCommentsUpdateDto();

        mockMvcTestHelper.requestAndAssertStatusIsOk(requestBuilder, requestDto);
    }

    private Long saveComment() {
        Long commentId = testDB.saveComments(post.getId(), user);
        assertOptionalAndGetEntity(commentsRepository::findById, commentId);
        return commentId;
    }

    private CommentsUpdateDto createCommentsUpdateDto() {
        String content = "updated comment";
        return new CommentsUpdateDto(content);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteComments() throws Exception {
        Long commentId = saveComment();

        MockHttpServletRequestBuilder requestBuilder = delete("/api/comments/{id}", commentId);

        mockMvcTestHelper.requestAndAssertStatusIsOk(requestBuilder, null);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteSelectedComments() throws Exception {
        List<Long> requestDto = createCommentIds();
        MockHttpServletRequestBuilder requestBuilder = delete("/api/comments/selected-delete");

        mockMvcTestHelper.requestAndAssertStatusIsOk(requestBuilder, requestDto);
    }

    private List<Long> createCommentIds() {
        List<Long> requestDto = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Long commentId = saveComment();
            requestDto.add(commentId);
        }
        return requestDto;
    }
}
