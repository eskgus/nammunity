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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = PostsApiController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CustomControllerAdvice.class))
@Import(MockMvcTestHelper.class)
@MockBeans({ @MockBean(WebConfig.class), @MockBean(PostsAuthInterceptor.class), @MockBean(CommentsAuthInterceptor.class) })
public class PostsApiControllerUnitTest {
    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @MockBean
    private PostsService postsService;

    private static final String TITLE = "title";
    private static final String CONTENT = "content";
    private static final Long ID = 1L;

    private static final String REQUEST_MAPPING = "/api/posts";

    @Test
    @WithMockUser
    public void savePosts() throws Exception {
        // given
        PostsSaveDto requestDto = PostsSaveDto.builder().title(TITLE).content(CONTENT).build();

        when(postsService.save(any(PostsSaveDto.class), any(Principal.class))).thenReturn(1L);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = post("/api/posts");
        mockMvcTestHelper.requestAndAssertStatusIsOk(requestBuilder, requestDto);

        verify(postsService).save(any(PostsSaveDto.class), any(Principal.class));
    }

    @Test
    @WithMockUser
    public void updatePosts() throws Exception {
        // given
        PostsUpdateDto requestDto = PostsUpdateDto.builder().title(TITLE).content(CONTENT).build();

        when(postsService.update(anyLong(), any(PostsUpdateDto.class))).thenReturn(ID);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = put(REQUEST_MAPPING + "/{id}", ID);
        mockMvcTestHelper.requestAndAssertStatusIsOk(requestBuilder, requestDto);

        verify(postsService).update(eq(ID), any(PostsUpdateDto.class));
    }

    @Test
    @WithMockUser
    public void deletePosts() throws Exception {
        // given
        doNothing().when(postsService).delete(anyLong());

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/{id}", ID);
        mockMvcTestHelper.requestAndAssertStatusIsOk(requestBuilder, null);

        verify(postsService).delete(eq(ID));
    }

    @Test
    @WithMockUser
    public void deleteSelectedPosts() throws Exception {
        // given
        List<Long> requestDto = Arrays.asList(ID, ID + 1, ID + 2);

        doNothing().when(postsService).deleteSelectedPosts(anyList());

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/selected-delete");
        mockMvcTestHelper.requestAndAssertStatusIsOk(requestBuilder, requestDto);

        verify(postsService).deleteSelectedPosts(eq(requestDto));
    }
}
