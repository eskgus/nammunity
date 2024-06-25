package com.eskgus.nammunity.web.likes;

import com.eskgus.nammunity.config.WebConfig;
import com.eskgus.nammunity.config.interceptor.CommentsAuthInterceptor;
import com.eskgus.nammunity.config.interceptor.PostsAuthInterceptor;
import com.eskgus.nammunity.handler.CustomControllerAdvice;
import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.service.likes.LikesService;
import com.eskgus.nammunity.web.controller.api.likes.LikesApiController;
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
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.ILLEGAL_ARGUMENT_EXCEPTION_TEST;
import static com.eskgus.nammunity.domain.enums.Fields.POSTS_ID;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = LikesApiController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CustomControllerAdvice.class))
@Import(MockMvcTestHelper.class)
@MockBeans({ @MockBean(WebConfig.class), @MockBean(PostsAuthInterceptor.class), @MockBean(CommentsAuthInterceptor.class) })
public class LikesApiControllerExceptionUnitTest {
    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @MockBean
    private LikesService likesService;

    private static final Long ID = 1L;

    private static final String REQUEST_MAPPING = "/api/likes";

    @Test
    @WithMockUser
    public void saveLikesThrowsIllegalArgumentException() throws Exception {
        // given
        when(likesService.save(anyLong(), isNull(), any(Principal.class)))
                .thenThrow(new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage()));

        // when/then
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING);
        performAndExpectBadRequestWithParam(requestBuilder);

        verify(likesService).save(eq(ID), isNull(), any(Principal.class));
    }

    @Test
    @WithMockUser
    public void deleteLikesThrowsIllegalArgumentException() throws Exception {
        // given
        doThrow(new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage()))
                .when(likesService).deleteByContentId(anyLong(), isNull(), any(Principal.class));

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING);
        performAndExpectBadRequestWithParam(requestBuilder);

        verify(likesService).deleteByContentId(eq(ID), isNull(), any(Principal.class));
    }

    @Test
    @WithMockUser
    public void deleteSelectedLikesThrowsIllegalArgumentException() throws Exception {
        // given
        List<Long> requestDto = Collections.emptyList();

        doThrow(new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage()))
                .when(likesService).deleteSelectedLikes(anyList());

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/selected-delete");
        ResultMatcher resultMatcher = createResultMatcher();
        mockMvcTestHelper.performAndExpectBadRequest(requestBuilder, requestDto, resultMatcher);

        verify(likesService).deleteSelectedLikes(eq(requestDto));
    }

    private ResultMatcher createResultMatcher() {
        return mockMvcTestHelper.createResultMatcher(ILLEGAL_ARGUMENT_EXCEPTION_TEST);
    }

    private void performAndExpectBadRequestWithParam(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        ResultMatcher resultMatcher = createResultMatcher();
        mockMvcTestHelper.performAndExpectBadRequestWithParam(requestBuilder, POSTS_ID, ID, resultMatcher);
    }
}
