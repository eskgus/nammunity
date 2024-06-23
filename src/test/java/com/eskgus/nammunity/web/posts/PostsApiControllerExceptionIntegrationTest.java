package com.eskgus.nammunity.web.posts;

import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.enums.Fields;
import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.posts.PostsSaveDto;
import com.eskgus.nammunity.web.dto.posts.PostsUpdateDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.*;
import java.util.function.Function;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static com.eskgus.nammunity.domain.enums.Fields.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostsApiControllerExceptionIntegrationTest {
    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    private User user;

    private static final String TEN_CHAR_STRING = "10 letters";

    private static final String REQUEST_MAPPING = "/api/posts";

    @BeforeEach
    public void setUp() {
        Long userId = testDataHelper.signUp(1L, Role.USER);
        this.user = assertOptionalAndGetEntity(userRepository::findById, userId);
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    @WithAnonymousUser
    public void savePostsWithAnonymousUser() throws Exception {
        // given
        PostsSaveDto requestDto = createPostsSaveDto(null, null);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING);
        performAndExpectNotBadRequest(requestBuilder, requestDto, UNAUTHORIZED);
    }

    @Test
    @WithMockUser(username = "username1")
    public void savePostsWithEmptyTitle() throws Exception {
        String title = "";
        ResultMatcher[] resultMatchers = createResultMatchers(TITLE, title, EMPTY_TITLE);
        testSavePostsExpectBadRequest(TITLE, title, resultMatchers);
    }

    @Test
    @WithMockUser(username = "username1")
    public void savePostsWithInvalidTitleLength() throws Exception {
        String title = TEN_CHAR_STRING.repeat(10) + "!";
        ResultMatcher[] resultMatchers = createResultMatchers(TITLE, title, INVALID_TITLE);
        testSavePostsExpectBadRequest(TITLE, title, resultMatchers);
    }

    @Test
    @WithMockUser(username = "username1")
    public void savePostsWithEmptyContent() throws Exception {
        String content = "";
        ResultMatcher[] resultMatchers = createResultMatchers(CONTENT, content, EMPTY_CONTENT);
        testSavePostsExpectBadRequest(CONTENT, content, resultMatchers);
    }

    @Test
    @WithMockUser(username = "username1")
    public void savePostsWithInvalidContentLength() throws Exception {
        String content = TEN_CHAR_STRING.repeat(300) + "!";
        ResultMatcher[] resultMatchers = createResultMatchers(CONTENT, content, INVALID_CONTENT);
        testSavePostsExpectBadRequest(CONTENT, content, resultMatchers);
    }

    @Test
    @WithMockUser(username = "username2")
    public void savePostsWithNonExistentUsername() throws Exception {
        ResultMatcher resultMatcher = createResultMatcher(NON_EXISTENT_USER);
        testSavePostsExpectBadRequest(null, null, resultMatcher);
    }

    @Test
    @WithAnonymousUser
    public void updatePostsWithAnonymousUser() throws Exception {
        testUpdatePostsExpectNotBadRequest(UNAUTHORIZED);
    }

    @Test
    @WithMockUser(username = "username2")
    public void updatePostsWithNonExistentUsername() throws Exception {
        ResultMatcher resultMatcher = createResultMatcher(NON_EXISTENT_USER);
        testUpdatePostsExpectBadRequest(true, null, null, resultMatcher);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updatePostsWithNonExistentPostId() throws Exception {
        ResultMatcher resultMatcher = createResultMatcher(NON_EXISTENT_POST);
        testUpdatePostsExpectBadRequest(false, null, null, resultMatcher);
    }

    @Test
    @WithMockUser(username = "username2")
    public void updatePostsWithForbiddenUser() throws Exception {
        saveUser();
        testUpdatePostsExpectNotBadRequest(FORBIDDEN);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updatePostsWithEmptyTitle() throws Exception {
        String title = "";
        ResultMatcher[] resultMatchers = createResultMatchers(TITLE, title, EMPTY_TITLE);
        testUpdatePostsExpectBadRequest(true, TITLE, title, resultMatchers);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updatePostsWithInvalidTitleLength() throws Exception {
        String title = TEN_CHAR_STRING.repeat(10) + "!";
        ResultMatcher[] resultMatchers = createResultMatchers(TITLE, title, INVALID_TITLE);
        testUpdatePostsExpectBadRequest(true, TITLE, title, resultMatchers);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updatePostsWithEmptyContent() throws Exception {
        String content = "";
        ResultMatcher[] resultMatchers = createResultMatchers(CONTENT, content, EMPTY_CONTENT);
        testUpdatePostsExpectBadRequest(true, CONTENT, content, resultMatchers);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updatePostsWithInvalidContentLength() throws Exception {
        String content = TEN_CHAR_STRING.repeat(300) + "!";
        ResultMatcher[] resultMatchers = createResultMatchers(CONTENT, content, INVALID_CONTENT);
        testUpdatePostsExpectBadRequest(true, CONTENT, content, resultMatchers);
    }

    @Test
    @WithAnonymousUser
    public void deletePostsWithAnonymousUser() throws Exception {
        testDeletePostsExpectNotBadRequest(UNAUTHORIZED);
    }

    @Test
    @WithMockUser(username = "username2")
    public void deletePostsWithNonExistentUsername() throws Exception {
        testDeletePostsExpectBadRequest(true, NON_EXISTENT_USER);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deletePostsWithNonExistentPostId() throws Exception {
        testDeletePostsExpectBadRequest(false, NON_EXISTENT_POST);
    }

    @Test
    @WithMockUser(username = "username2")
    public void deletePostsWithForbiddenUser() throws Exception {
        saveUser();
        testDeletePostsExpectNotBadRequest(FORBIDDEN);
    }

    @Test
    @WithAnonymousUser
    public void deleteSelectedPostsWithAnonymousUser() throws Exception {
        // given
        List<Long> requestDto = createPostIds();

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/selected-delete");
        performAndExpectNotBadRequest(requestBuilder, requestDto, UNAUTHORIZED);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteSelectedPostsWithEmptyPostIds() throws Exception {
        // given
        List<Long> requestDto = Collections.emptyList();

        // when/then
        testDeleteSelectedPostsExpectBadRequest(requestDto, EMPTY_CONTENT_IDS);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteSelectedPostsWithNonExistentPostId() throws Exception {
        // given
        List<Long> requestDto = createPostIds();

        // when/then
        testDeleteSelectedPostsExpectBadRequest(requestDto, NON_EXISTENT_POST);
    }

    private void testSavePostsExpectBadRequest(Fields field, String value, ResultMatcher... resultMatcher) throws Exception {
        // given
        PostsSaveDto requestDto = createPostsSaveDto(field, value);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING);
        performAndExpectBadRequest(requestBuilder, requestDto, resultMatcher);
    }

    private void testUpdatePostsExpectBadRequest(boolean doesPostExist, Fields field, String value,
                                                 ResultMatcher... resultMatchers) throws Exception {
        // given
        Long postId = doesPostExist ? savePost() : postsRepository.count() + 1;

        PostsUpdateDto requestDto = createPostsUpdateDto(field, value);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = put(REQUEST_MAPPING + "/{id}", postId);
        performAndExpectBadRequest(requestBuilder, requestDto, resultMatchers);
    }

    private void testDeletePostsExpectBadRequest(boolean doesPostExist, ExceptionMessages exceptionMessage) throws Exception {
        // given
        Long postId = doesPostExist ? savePost() : postsRepository.count() + 1;

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/{id}", postId);
        ResultMatcher resultMatcher = createResultMatcher(exceptionMessage);
        performAndExpectBadRequest(requestBuilder, null, resultMatcher);
    }

    private void testDeleteSelectedPostsExpectBadRequest(List<Long> requestDto, ExceptionMessages exceptionMessage) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/selected-delete");
        ResultMatcher resultMatcher = createResultMatcher(exceptionMessage);
        performAndExpectBadRequest(requestBuilder, requestDto, resultMatcher);
    }

    private void testUpdatePostsExpectNotBadRequest(ExceptionMessages exceptionMessage) throws Exception {
        // given
        Long postId = savePost();

        PostsUpdateDto requestDto = createPostsUpdateDto(null, null);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = put(REQUEST_MAPPING + "/{id}", postId);
        performAndExpectNotBadRequest(requestBuilder, requestDto, exceptionMessage);
    }

    private void testDeletePostsExpectNotBadRequest(ExceptionMessages exceptionMessage) throws Exception {
        // given
        Long postId = savePost();

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/{id}", postId);
        performAndExpectNotBadRequest(requestBuilder, null, exceptionMessage);
    }

    private void saveUser() {
        Long userId = testDataHelper.signUp(user.getId() + 1, Role.USER);
        assertOptionalAndGetEntity(userRepository::findById, userId);
    }

    private Long savePost() {
        Long postId = testDataHelper.savePosts(user);
        assertOptionalAndGetEntity(postsRepository::findById, postId);

        return postId;
    }

    private PostsSaveDto createPostsSaveDto(Fields field, String value) {
        Pair<String, String> pair = initializeTitleAndContent(field, value);

        return PostsSaveDto.builder().title(pair.getFirst()).content(pair.getSecond()).build();
    }

    private PostsUpdateDto createPostsUpdateDto(Fields field, String value) {
        Pair<String, String> pair = initializeTitleAndContent(field, value);

        return PostsUpdateDto.builder().title(pair.getFirst()).content(pair.getSecond()).build();
    }

    private List<Long> createPostIds() {
        long count = postsRepository.count();

        return Arrays.asList(count + 1, count + 2, count + 3);
    }

    private Pair<String, String> initializeTitleAndContent(Fields field, String value) {
        String title = TEN_CHAR_STRING;
        String content = TEN_CHAR_STRING;

        if (TITLE.equals(field)) {
            title = value;
        } else if (CONTENT.equals(field)) {
            content = value;
        }

        return Pair.of(title, content);
    }

    private ResultMatcher[] createResultMatchers(Fields field, String rejectedValue, ExceptionMessages exceptionMessage) {
        return mockMvcTestHelper.createResultMatchers(field, rejectedValue, exceptionMessage);
    }

    private ResultMatcher createResultMatcher(ExceptionMessages exceptionMessage) {
        return mockMvcTestHelper.createResultMatcher(exceptionMessage);
    }

    private <T> void performAndExpectNotBadRequest(MockHttpServletRequestBuilder requestBuilder, T requestDto,
                                                   ExceptionMessages exceptionMessage) throws Exception {
        if (UNAUTHORIZED.equals(exceptionMessage)) {
            mockMvcTestHelper.performAndExpectUnauthorized(requestBuilder, requestDto);
        } else if (FORBIDDEN.equals(exceptionMessage)) {
            mockMvcTestHelper.performAndExpectForbidden(requestBuilder, requestDto);
        }
    }

    private <T> void performAndExpectBadRequest(MockHttpServletRequestBuilder requestBuilder, T requestDto,
                                                ResultMatcher... resultMatchers) throws Exception {
        mockMvcTestHelper.performAndExpectBadRequest(requestBuilder, requestDto, resultMatchers);
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }
}
