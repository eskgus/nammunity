package com.eskgus.nammunity.web.likes;

import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LikesApiControllerIntegrationTest {
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

    @Autowired
    private LikesRepository likesRepository;

    private User user;
    private Long postId;
    private Long commentId;

    private static final String POST_ID = "postsId";
    private static final String COMMENT_ID = "commentsId";

    private static final String REQUEST_MAPPING = "/api/likes";

    @BeforeEach
    public void setUp() {
        Long userId = testDataHelper.signUp(1L, Role.USER);
        this.user = assertOptionalAndGetEntity(userRepository::findById, userId);

        Long postId = testDataHelper.savePosts(user);
        assertOptionalAndGetEntity(postsRepository::findById, postId);
        this.postId = postId;

        Long commentId = testDataHelper.saveComments(postId, user);
        assertOptionalAndGetEntity(commentsRepository::findById, commentId);
        this.commentId = commentId;
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    @WithMockUser(username = "username1")
    public void savePostLikes() throws Exception {
        testSaveLikes(POST_ID, postId);
    }

    @Test
    @WithMockUser(username = "username1")
    public void saveCommentLikes() throws Exception {
        testSaveLikes(COMMENT_ID, commentId);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deletePostLikes() throws Exception {
        testDeleteLikes(POST_ID, postId);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteCommentLikes() throws Exception {
        testDeleteLikes(COMMENT_ID, commentId);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteSelectedLikes() throws Exception {
        List<Long> requestDto = createLikeIds();
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/selected-delete");
        mockMvcTestHelper.performAndExpectOk(requestBuilder, requestDto);
    }

    private void testSaveLikes(String name, Long value) throws Exception {
        // given
        // when/then
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING);
        performAndExpectOkWithParam(requestBuilder, name, value);
    }

    private void testDeleteLikes(String name, Long value) throws Exception {
        // given
        saveLike(name);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING);
        performAndExpectOkWithParam(requestBuilder, name, value);
    }

    private List<Long> createLikeIds() {
        List<Long> requestDto = new ArrayList<>();
        requestDto.add(saveLike(POST_ID));
        requestDto.add(saveLike(COMMENT_ID));

        return requestDto;
    }

    private Long saveLike(String name) {
        if (POST_ID.equals(name)) {
            Long postLikeId = testDataHelper.savePostLikes(postId, user);
            assertOptionalAndGetEntity(likesRepository::findById, postLikeId);

            return postLikeId;
        } else {
            Long commentLikeId = testDataHelper.saveCommentLikes(commentId, user);
            assertOptionalAndGetEntity(likesRepository::findById, commentLikeId);

            return commentLikeId;
        }
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }

    private void performAndExpectOkWithParam(MockHttpServletRequestBuilder requestBuilder,
                                             String name, Long value) throws Exception {
        mockMvcTestHelper.performAndExpectOkWithParam(requestBuilder, name, value);
    }
}
