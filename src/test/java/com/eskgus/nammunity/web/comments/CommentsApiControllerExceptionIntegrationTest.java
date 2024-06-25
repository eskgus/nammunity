package com.eskgus.nammunity.web.comments;

import com.eskgus.nammunity.domain.enums.ExceptionMessages;
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
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.*;
import java.util.function.Function;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static com.eskgus.nammunity.domain.enums.Fields.CONTENT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommentsApiControllerExceptionIntegrationTest {
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

    private static final String TEN_CHAR_STRING = "10 letters";

    private static final String REQUEST_MAPPING = "/api/comments";

    @BeforeEach
    public void setUp() {
        Long userId = testDataHelper.signUp(1L, Role.USER);
        this.user = assertOptionalAndGetEntity(userRepository::findById, userId);

        Long postId = testDataHelper.savePosts(user);
        assertOptionalAndGetEntity(postsRepository::findById, postId);
        this.postId = postId;
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    @WithAnonymousUser
    public void saveCommentsWithAnonymousUser() throws Exception {
        // given
        CommentsSaveDto requestDto = createCommentsSaveDto(true, TEN_CHAR_STRING);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING);
        performAndExpectNotBadRequest(requestBuilder, requestDto, UNAUTHORIZED);
    }

    @Test
    @WithMockUser(username = "username1")
    public void saveCommentsWithEmptyContent() throws Exception {
        String content = "";
        ResultMatcher[] resultMatchers = createResultMatchers(content, EMPTY_COMMENT);
        testSaveCommentsExpectBadRequest(true, content, resultMatchers);
    }

    @Test
    @WithMockUser(username = "username1")
    public void saveCommentsWithInvalidContentLength() throws Exception {
        String content = TEN_CHAR_STRING.repeat(150) + "!";
        ResultMatcher[] resultMatchers = createResultMatchers(content, INVALID_COMMENT);
        testSaveCommentsExpectBadRequest(true, content, resultMatchers);
    }

    @Test
    @WithMockUser(username = "username2")
    public void saveCommentsWithNonExistentUsername() throws Exception {
        ResultMatcher resultMatcher = createResultMatcher(USERNAME_NOT_FOUND);
        testSaveCommentsExpectBadRequest(true, TEN_CHAR_STRING, resultMatcher);
    }

    @Test
    @WithMockUser(username = "username1")
    public void saveCommentsWithNonExistentPostId() throws Exception {
        ResultMatcher resultMatcher = createResultMatcher(POST_NOT_FOUND);
        testSaveCommentsExpectBadRequest(false, TEN_CHAR_STRING, resultMatcher);
    }

    @Test
    @WithAnonymousUser
    public void updateCommentsWithAnonymousUser() throws Exception {
        testUpdateCommentsExpectNotBadRequest(UNAUTHORIZED);
    }

    @Test
    @WithMockUser(username = "username2")
    public void updateCommentsWithNonExistentUsername() throws Exception {
        ResultMatcher resultMatcher = createResultMatcher(USERNAME_NOT_FOUND);
        testUpdateCommentsExpectBadRequest(true, TEN_CHAR_STRING, resultMatcher);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updateCommentsWithNonExistentCommentId() throws Exception {
        ResultMatcher resultMatcher = createResultMatcher(COMMENT_NOT_FOUND);
        testUpdateCommentsExpectBadRequest(false, TEN_CHAR_STRING, resultMatcher);
    }

    @Test
    @WithMockUser(username = "username2")
    public void updateCommentsWithForbiddenUser() throws Exception {
        saveUser();
        testUpdateCommentsExpectNotBadRequest(FORBIDDEN);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updateCommentsWithEmptyContent() throws Exception {
        String content = "";
        ResultMatcher[] resultMatchers = createResultMatchers(content, EMPTY_COMMENT);
        testUpdateCommentsExpectBadRequest(true, content, resultMatchers);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updateCommentsWithInvalidContentLength() throws Exception {
        String content = TEN_CHAR_STRING.repeat(150) + "!";
        ResultMatcher[] resultMatchers = createResultMatchers(content, INVALID_COMMENT);
        testUpdateCommentsExpectBadRequest(true, content, resultMatchers);
    }

    @Test
    @WithAnonymousUser
    public void deleteCommentsWithAnonymousUser() throws Exception {
        testDeleteCommentsExpectNotBadRequest(UNAUTHORIZED);
    }

    @Test
    @WithMockUser(username = "username2")
    public void deleteCommentsWithNonExistentUsername() throws Exception {
        testDeleteCommentsExpectBadRequest(true, USERNAME_NOT_FOUND);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteCommentsWithNonExistentCommentId() throws Exception {
        testDeleteCommentsExpectBadRequest(false, COMMENT_NOT_FOUND);
    }

    @Test
    @WithMockUser(username = "username2")
    public void deleteCommentsWithForbiddenUser() throws Exception {
        saveUser();
        testDeleteCommentsExpectNotBadRequest(FORBIDDEN);
    }

    @Test
    @WithAnonymousUser
    public void deleteSelectedCommentsWithAnonymousUser() throws Exception {
        // given
        List<Long> requestDto = createCommentIds();

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/selected-delete");
        performAndExpectNotBadRequest(requestBuilder, requestDto, UNAUTHORIZED);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteSelectedCommentsWithEmptyCommentIds() throws Exception {
        // given
        List<Long> requestDto = Collections.emptyList();

        // when/then
        testDeleteSelectedCommentsExpectBadRequest(requestDto, EMPTY_CONTENT_IDS);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteSelectedCommentsWithNonExistentCommentId() throws Exception {
        // given
        List<Long> requestDto = createCommentIds();

        // when/then
        testDeleteSelectedCommentsExpectBadRequest(requestDto, COMMENT_NOT_FOUND);
    }

    private void testSaveCommentsExpectBadRequest(boolean doesPostExist, String content,
                                                  ResultMatcher... resultMatchers) throws Exception {
        // given
        CommentsSaveDto requestDto = createCommentsSaveDto(doesPostExist, content);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING);
        performAndExpectBadRequest(requestBuilder, requestDto, resultMatchers);
    }

    private void testUpdateCommentsExpectBadRequest(boolean doesCommentExist, String content,
                                                    ResultMatcher... resultMatchers) throws Exception {
        // given
        Long commentId = doesCommentExist ? saveComment() : commentsRepository.count() + 1;

        CommentsUpdateDto requestDto = new CommentsUpdateDto(content);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = put(REQUEST_MAPPING + "/{id}", commentId);
        performAndExpectBadRequest(requestBuilder, requestDto, resultMatchers);
    }

    private void testDeleteCommentsExpectBadRequest(boolean doesCommentExist, ExceptionMessages exceptionMessage) throws Exception {
        // given
        Long commentId = doesCommentExist ? saveComment() : commentsRepository.count() + 1;

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/{id}", commentId);
        ResultMatcher resultMatcher = createResultMatcher(exceptionMessage);
        performAndExpectBadRequest(requestBuilder, null, resultMatcher);
    }

    private void testDeleteSelectedCommentsExpectBadRequest(List<Long> requestDto, ExceptionMessages exceptionMessage) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/selected-delete");
        ResultMatcher resultMatcher = createResultMatcher(exceptionMessage);
        performAndExpectBadRequest(requestBuilder, requestDto, resultMatcher);
    }

    private void testUpdateCommentsExpectNotBadRequest(ExceptionMessages exceptionMessage) throws Exception {
        // given
        Long commentId = saveComment();

        CommentsUpdateDto requestDto = new CommentsUpdateDto(TEN_CHAR_STRING);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = put(REQUEST_MAPPING + "/{id}", commentId);
        performAndExpectNotBadRequest(requestBuilder, requestDto, exceptionMessage);
    }

    private void testDeleteCommentsExpectNotBadRequest(ExceptionMessages exceptionMessage) throws Exception {
        // given
        Long commentId = saveComment();

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/{id}", commentId);
        performAndExpectNotBadRequest(requestBuilder, null, exceptionMessage);
    }

    private <T> void performAndExpectBadRequest(MockHttpServletRequestBuilder requestBuilder, T requestDto,
                                                ResultMatcher... resultMatchers) throws Exception {
        mockMvcTestHelper.performAndExpectBadRequest(requestBuilder, requestDto, resultMatchers);
    }

    private <T> void performAndExpectNotBadRequest(MockHttpServletRequestBuilder requestBuilder, T requestDto,
                                                   ExceptionMessages exceptionMessage) throws Exception {
        if (UNAUTHORIZED.equals(exceptionMessage)) {
            mockMvcTestHelper.performAndExpectUnauthorized(requestBuilder, requestDto);
        } else if (FORBIDDEN.equals(exceptionMessage)) {
            mockMvcTestHelper.performAndExpectForbidden(requestBuilder, requestDto);
        }
    }

    private void saveUser() {
        Long userId = testDataHelper.signUp(user.getId() + 1, Role.USER);
        assertOptionalAndGetEntity(userRepository::findById, userId);
    }

    private Long saveComment() {
        Long commentId = testDataHelper.saveComments(postId, user);
        assertOptionalAndGetEntity(commentsRepository::findById, commentId);

        return commentId;
    }

    private CommentsSaveDto createCommentsSaveDto(boolean doesPostExist, String content) {
        Long postId = doesPostExist ? this.postId : this.postId + 1;

        return new CommentsSaveDto(content, postId);
    }

    private List<Long> createCommentIds() {
        long count = commentsRepository.count();

        return Arrays.asList(count + 1, count + 2, count + 3);
    }

    private ResultMatcher[] createResultMatchers(String rejectedValue,
                                                 ExceptionMessages exceptionMessage) {
        return mockMvcTestHelper.createResultMatchers(CONTENT, rejectedValue, exceptionMessage);
    }

    private ResultMatcher createResultMatcher(ExceptionMessages exceptionMessage) {
        return mockMvcTestHelper.createResultMatcher(exceptionMessage);
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }
}
