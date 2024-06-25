package com.eskgus.nammunity.web.likes;

import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.enums.Fields;
import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.UserRepository;
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

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static com.eskgus.nammunity.domain.enums.Fields.COMMENTS_ID;
import static com.eskgus.nammunity.domain.enums.Fields.POSTS_ID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LikesApiControllerExceptionIntegrationTest {
    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LikesRepository likesRepository;

    private static final Long ID = 1L;

    private static final String REQUEST_MAPPING = "/api/likes";

    @BeforeEach
    public void setUp() {
        Long userId = testDataHelper.signUp(1L, Role.USER);
        testDataHelper.assertOptionalAndGetEntity(userRepository::findById, userId);
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    @WithAnonymousUser
    public void saveLikesWithAnonymousUser() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING);
        performAndExpectNotBadRequestWithParam(requestBuilder);
    }

    @Test
    @WithMockUser(username = "username2")
    public void saveLikesWithNonExistentUsername() throws Exception {
        testSaveLikesExpectBadRequest(POSTS_ID, USERNAME_NOT_FOUND);
    }

    @Test
    @WithMockUser(username = "username1")
    public void savePostLikesWithNonExistentPost() throws Exception {
        testSaveLikesExpectBadRequest(POSTS_ID, POST_NOT_FOUND);
    }

    @Test
    @WithMockUser(username = "username1")
    public void saveCommentLikesWithNonExistentComment() throws Exception {
        testSaveLikesExpectBadRequest(COMMENTS_ID, COMMENT_NOT_FOUND);
    }

    @Test
    @WithAnonymousUser
    public void deleteLikesWithAnonymousUser() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING);
        performAndExpectNotBadRequestWithParam(requestBuilder);
    }

    @Test
    @WithMockUser(username = "username2")
    public void deleteLikesWithNonExistentUsername() throws Exception {
        testDeleteLikesExpectBadRequest(POSTS_ID, USERNAME_NOT_FOUND);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deletePostLikesWithNonExistentPost() throws Exception {
        testDeleteLikesExpectBadRequest(POSTS_ID, POST_NOT_FOUND);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteCommentLikesWithNonExistentComment() throws Exception {
        testDeleteLikesExpectBadRequest(COMMENTS_ID, COMMENT_NOT_FOUND);
    }

    @Test
    @WithAnonymousUser
    public void deleteSelectedLikesWithAnonymousUser() throws Exception {
        // given
        List<Long> requestDto = createLikeIds();

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/selected-delete");
        mockMvcTestHelper.performAndExpectUnauthorized(requestBuilder, requestDto);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteSelectedLikesWithEmptyLikeIds() throws Exception {
        // given
        List<Long> requestDto = Collections.emptyList();

        // when/then
        testDeleteSelectedLikesExpectBadRequest(requestDto, EMPTY_CONTENT_IDS);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteSelectedLikesWithNonExistentLike() throws Exception {
        // given
        List<Long> requestDto = createLikeIds();

        // when/then
        testDeleteSelectedLikesExpectBadRequest(requestDto, LIKE_NOT_FOUND);
    }

    private List<Long> createLikeIds() {
        long count = likesRepository.count();

        return Arrays.asList(count + 1, count + 2, count + 3);
    }

    private void testSaveLikesExpectBadRequest(Fields field, ExceptionMessages exceptionMessage) throws Exception {
        // given
        // when/then
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING);
        ResultMatcher resultMatcher = createResultMatcher(exceptionMessage);
        performAndExpectBadRequestWithParam(requestBuilder, field, resultMatcher);
    }

    private void testDeleteLikesExpectBadRequest(Fields field, ExceptionMessages exceptionMessage) throws Exception {
        // given
        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING);
        ResultMatcher resultMatcher = createResultMatcher(exceptionMessage);
        performAndExpectBadRequestWithParam(requestBuilder, field, resultMatcher);
    }

    private void testDeleteSelectedLikesExpectBadRequest(List<Long> requestDto, ExceptionMessages exceptionMessage) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/selected-delete");
        ResultMatcher resultMatcher = createResultMatcher(exceptionMessage);
        mockMvcTestHelper.performAndExpectBadRequest(requestBuilder, requestDto, resultMatcher);
    }

    private ResultMatcher createResultMatcher(ExceptionMessages exceptionMessage) {
        return mockMvcTestHelper.createResultMatcher(exceptionMessage);
    }

    private void performAndExpectNotBadRequestWithParam(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        mockMvcTestHelper.performAndExpectUnauthorizedWithParam(requestBuilder, POSTS_ID, ID);
    }

    private void performAndExpectBadRequestWithParam(MockHttpServletRequestBuilder requestBuilder,
                                                     Fields field, ResultMatcher resultMatcher) throws Exception {
        mockMvcTestHelper.performAndExpectBadRequestWithParam(requestBuilder, field, ID, resultMatcher);
    }
}
