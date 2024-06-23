package com.eskgus.nammunity.web.comments;

import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
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
public class CommentsApiControllerIntegrationTest {
    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    private User user;
    private Long postId;

    private static final String COMMENT = "comment";

    private static final String REQUEST_MAPPING = "/api/comments";

    @BeforeEach
    public void setUp() {
        Long userId = testDataHelper.signUp(1L, Role.USER);
        this.user = assertOptionalAndGetEntity(userRepository::findById, userId);

        Long postId = testDataHelper.savePosts(user);
        assertOptionalAndGetEntity(postsRepository::findById, postId);
        this.postId = postId;
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    @WithMockUser(username = "username1")
    public void saveComments() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING);
        CommentsSaveDto requestDto = createCommentsSaveDto();

        performAndExpectOk(requestBuilder, requestDto);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updateComments() throws Exception {
        Long commentId = saveComment();

        MockHttpServletRequestBuilder requestBuilder = put(REQUEST_MAPPING + "/{id}", commentId);
        CommentsUpdateDto requestDto = createCommentsUpdateDto();

        performAndExpectOk(requestBuilder, requestDto);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteComments() throws Exception {
        Long commentId = saveComment();

        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/{id}", commentId);

        performAndExpectOk(requestBuilder, null);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteSelectedComments() throws Exception {
        List<Long> requestDto = createCommentIds();
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/selected-delete");

        performAndExpectOk(requestBuilder, requestDto);
    }

    private CommentsSaveDto createCommentsSaveDto() {
        return new CommentsSaveDto(COMMENT, postId);
    }

    private CommentsUpdateDto createCommentsUpdateDto() {
        String content = "updated " + COMMENT;

        return new CommentsUpdateDto(content);
    }

    private List<Long> createCommentIds() {
        List<Long> requestDto = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Long commentId = saveComment();
            requestDto.add(commentId);
        }

        return requestDto;
    }

    private Long saveComment() {
        Long commentId = testDataHelper.saveComments(postId, user);
        assertOptionalAndGetEntity(commentsRepository::findById, commentId);

        return commentId;
    }

    private <T> void performAndExpectOk(MockHttpServletRequestBuilder requestBuilder, T requestDto) throws Exception {
        mockMvcTestHelper.performAndExpectOk(requestBuilder, requestDto);
    }
}
