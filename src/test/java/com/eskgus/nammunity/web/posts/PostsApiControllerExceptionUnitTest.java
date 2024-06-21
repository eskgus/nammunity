package com.eskgus.nammunity.web.posts;

import com.eskgus.nammunity.config.WebConfig;
import com.eskgus.nammunity.config.interceptor.CommentsAuthInterceptor;
import com.eskgus.nammunity.config.interceptor.PostsAuthInterceptor;
import com.eskgus.nammunity.handler.CustomControllerAdvice;
import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.web.controller.api.posts.PostsApiController;
import com.eskgus.nammunity.web.dto.posts.PostsSaveDto;
import com.eskgus.nammunity.web.dto.posts.PostsUpdateDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.verification.VerificationMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = PostsApiController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CustomControllerAdvice.class))
@Import(MockMvcTestHelper.class)
@MockBeans({ @MockBean(WebConfig.class), @MockBean(PostsAuthInterceptor.class), @MockBean(CommentsAuthInterceptor.class) })
public class PostsApiControllerExceptionUnitTest {
    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @MockBean
    private PostsService postsService;

    private static final Long ID = 1L;
    private static final String TITLE = "title";
    private static final String CONTENT = "content";
    private static final String TEN_CHAR_STRING = "10 letters";

    private static final String REQUEST_MAPPING = "/api/posts";

    @Test
    @WithMockUser
    public void savePostsWithEmptyTitle() throws Exception {
        String title = "";
        ResultMatcher[] resultMatchers = createResultMatchers(TITLE, EMPTY_TITLE.getMessage(), title);
        testSavePostsException(title, TEN_CHAR_STRING, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void savePostsWithInvalidTitleLength() throws Exception {
        String title = TEN_CHAR_STRING.repeat(10) + "!";
        ResultMatcher[] resultMatchers = createResultMatchers(TITLE, INVALID_TITLE.getMessage(), title);
        testSavePostsException(title, TEN_CHAR_STRING, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void savePostsWithEmptyContent() throws Exception {
        String content = "";
        ResultMatcher[] resultMatchers = createResultMatchers(CONTENT, EMPTY_CONTENT.getMessage(), content);
        testSavePostsException(TEN_CHAR_STRING, content, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void savePostsWithInvalidContentLength() throws Exception {
        String content = TEN_CHAR_STRING.repeat(300) + "!";
        ResultMatcher[] resultMatchers = createResultMatchers(CONTENT, INVALID_CONTENT.getMessage(), content);
        testSavePostsException(TEN_CHAR_STRING, content, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void savePostsThrowsIllegalArgumentException() throws Exception {
        when(postsService.save(any(PostsSaveDto.class), any(Principal.class)))
                .thenThrow(new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage()));

        ResultMatcher resultMatcher = createResultMatcher();
        testSavePostsException(TEN_CHAR_STRING, TEN_CHAR_STRING, times(1), resultMatcher);
    }

    @Test
    @WithMockUser
    public void updatePostsWithEmptyTitle() throws Exception {
        String title = "";
        ResultMatcher[] resultMatchers = createResultMatchers(TITLE, EMPTY_TITLE.getMessage(), title);
        testUpdatePostsException(title, TEN_CHAR_STRING, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void updatePostsWithInvalidTitleLength() throws Exception {
        String title = TEN_CHAR_STRING.repeat(100) + "!";
        ResultMatcher[] resultMatchers = createResultMatchers(TITLE, INVALID_TITLE.getMessage(), title);
        testUpdatePostsException(title, TEN_CHAR_STRING, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void updatePostsWithEmptyContent() throws Exception {
        String content = "";
        ResultMatcher[] resultMatchers = createResultMatchers(CONTENT, EMPTY_CONTENT.getMessage(), content);
        testUpdatePostsException(TEN_CHAR_STRING, content, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void updatePostsWithInvalidContentLength() throws Exception {
        String content = TEN_CHAR_STRING.repeat(300) + "!";
        ResultMatcher[] resultMatchers = createResultMatchers(CONTENT, INVALID_CONTENT.getMessage(), content);
        testUpdatePostsException(TEN_CHAR_STRING, content, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void updatePostsThrowsIllegalArgumentException() throws Exception {
        when(postsService.update(anyLong(), any(PostsUpdateDto.class)))
                .thenThrow(new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage()));

        ResultMatcher resultMatcher = createResultMatcher();
        testUpdatePostsException(TEN_CHAR_STRING, TEN_CHAR_STRING, times(1), resultMatcher);
    }

    @Test
    @WithMockUser
    public void deletePostsThrowsIllegalArgumentException() throws Exception {
        // given
        doThrow(new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage()))
                .when(postsService).delete(anyLong());

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/{id}", ID);
        ResultMatcher resultMatcher = createResultMatcher();
        mockMvcTestHelper.requestAndAssertStatusIsBadRequest(requestBuilder, null, resultMatcher);

        verify(postsService).delete(eq(ID));
    }

    @Test
    @WithMockUser
    public void deleteSelectedPostsThrowsIllegalArgumentException() throws Exception {
        // given
        List<Long> requestDto = Collections.emptyList();

        doThrow(new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage()))
                .when(postsService).deleteSelectedPosts(anyList());

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/selected-delete");
        ResultMatcher resultMatcher = createResultMatcher();
        mockMvcTestHelper.requestAndAssertStatusIsBadRequest(requestBuilder, requestDto, resultMatcher);

        verify(postsService).deleteSelectedPosts(eq(requestDto));
    }

    private ResultMatcher[] createResultMatchers(String expectedField, String expectedDefaultMessage,
                                                 String expectedRejectedValue) {
        return mockMvcTestHelper.createResultMatchers(expectedField, expectedDefaultMessage, expectedRejectedValue);
    }

    private ResultMatcher createResultMatcher() {
        return mockMvcTestHelper.createResultMatcher(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage());
    }

    private void testSavePostsException(String title, String content, VerificationMode mode,
                                        ResultMatcher... resultMatchers) throws Exception {
        // given
        PostsSaveDto requestDto = PostsSaveDto.builder().title(title).content(content).build();

        // when/then
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING);
        mockMvcTestHelper.requestAndAssertStatusIsBadRequest(requestBuilder, requestDto, resultMatchers);

        verify(postsService, mode).save(any(PostsSaveDto.class), any(Principal.class));
    }

    private void testUpdatePostsException(String title, String content, VerificationMode mode,
                                          ResultMatcher... resultMatchers) throws Exception {
        // given
        PostsUpdateDto requestDto = PostsUpdateDto.builder().title(title).content(content).build();

        // when/then
        MockHttpServletRequestBuilder requestBuilder = put(REQUEST_MAPPING + "/{id}", ID);
        mockMvcTestHelper.requestAndAssertStatusIsBadRequest(requestBuilder, requestDto, resultMatchers);

        verify(postsService, mode).update(eq(ID), any(PostsUpdateDto.class));
    }
}
