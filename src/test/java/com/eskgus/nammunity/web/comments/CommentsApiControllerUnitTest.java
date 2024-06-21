package com.eskgus.nammunity.web.comments;

import com.eskgus.nammunity.config.WebConfig;
import com.eskgus.nammunity.config.interceptor.CommentsAuthInterceptor;
import com.eskgus.nammunity.config.interceptor.PostsAuthInterceptor;
import com.eskgus.nammunity.handler.CustomControllerAdvice;
import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.web.controller.api.comments.CommentsApiController;
import com.eskgus.nammunity.web.dto.comments.CommentsSaveDto;
import com.eskgus.nammunity.web.dto.comments.CommentsUpdateDto;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = CommentsApiController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CustomControllerAdvice.class))
@Import(MockMvcTestHelper.class)
@MockBeans({ @MockBean(WebConfig.class), @MockBean(PostsAuthInterceptor.class), @MockBean(CommentsAuthInterceptor.class) })
public class CommentsApiControllerUnitTest {
    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @MockBean
    private CommentsService commentsService;

    private static final Long ID = 1L;

    private static final String REQUEST_MAPPING = "/api/comments";

    @Test
    @WithMockUser
    public void saveComments() throws Exception {
        // given
        CommentsSaveDto requestDto = new CommentsSaveDto("comment", ID);

        when(commentsService.save(any(CommentsSaveDto.class), any(Principal.class))).thenReturn(ID);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING);
        mockMvcTestHelper.requestAndAssertStatusIsOk(requestBuilder, requestDto);

        verify(commentsService).save(any(CommentsSaveDto.class), any(Principal.class));
    }

    @Test
    @WithMockUser
    public void updateComments() throws Exception {
        // given
        CommentsUpdateDto requestDto = new CommentsUpdateDto("updated comment");

        when(commentsService.update(anyLong(), anyString())).thenReturn(ID);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = put(REQUEST_MAPPING + "/{id}", ID);
        mockMvcTestHelper.requestAndAssertStatusIsOk(requestBuilder, requestDto);

        verify(commentsService).update(eq(ID), eq(requestDto.getContent()));
    }

    @Test
    @WithMockUser
    public void deleteComments() throws Exception {
        // given
        doNothing().when(commentsService).delete(anyLong());

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/{id}", ID);
        mockMvcTestHelper.requestAndAssertStatusIsOk(requestBuilder, null);

        verify(commentsService).delete(eq(ID));
    }

    @Test
    @WithMockUser
    public void deleteSelectedComments() throws Exception {
        // given
        List<Long> requestDto = Arrays.asList(ID, ID + 1, ID + 2);

        doNothing().when(commentsService).deleteSelectedComments(anyList());

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/selected-delete");
        mockMvcTestHelper.requestAndAssertStatusIsOk(requestBuilder, requestDto);

        verify(commentsService).deleteSelectedComments(eq(requestDto));
    }
}
