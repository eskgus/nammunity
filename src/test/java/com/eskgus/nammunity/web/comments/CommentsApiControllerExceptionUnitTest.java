package com.eskgus.nammunity.web.comments;

import com.eskgus.nammunity.config.WebConfig;
import com.eskgus.nammunity.config.interceptor.CommentsAuthInterceptor;
import com.eskgus.nammunity.config.interceptor.PostsAuthInterceptor;
import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.handler.CustomControllerAdvice;
import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.web.controller.api.comments.CommentsApiController;
import com.eskgus.nammunity.web.dto.comments.CommentsSaveDto;
import com.eskgus.nammunity.web.dto.comments.CommentsUpdateDto;
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
import static com.eskgus.nammunity.domain.enums.Fields.CONTENT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
public class CommentsApiControllerExceptionUnitTest {
    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @MockBean
    private CommentsService commentsService;

    private static final Long ID = 1L;
    private static final String TEN_CHAR_STRING = "10 letters";

    private static final String REQUEST_MAPPING = "/api/comments";

    @Test
    @WithMockUser
    public void saveCommentsWithEmptyComment() throws Exception {
        String content = "";
        ResultMatcher[] resultMatchers = createResultMatchers(content, EMPTY_COMMENT);
        testSaveCommentsException(content, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void saveCommentsWithInvalidComment() throws Exception {
        String content = TEN_CHAR_STRING.repeat(150) + "!";
        ResultMatcher[] resultMatchers = createResultMatchers(content, INVALID_COMMENT);
        testSaveCommentsException(content, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void saveCommentsThrowsIllegalArgumentException() throws Exception {
        when(commentsService.save(any(CommentsSaveDto.class), any(Principal.class)))
                .thenThrow(new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage()));

        ResultMatcher resultMatcher = createResultMatcher();
        testSaveCommentsException(TEN_CHAR_STRING, times(1), resultMatcher);
    }

    @Test
    @WithMockUser
    public void updateCommentsWithEmptyComment() throws Exception {
        String content = "";
        ResultMatcher[] resultMatchers = createResultMatchers(content, EMPTY_COMMENT);
        testUpdateCommentsException(content, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void updateCommentsWithInvalidComment() throws Exception {
        String content = TEN_CHAR_STRING.repeat(150) + "!";
        ResultMatcher[] resultMatchers = createResultMatchers(content, INVALID_COMMENT);
        testUpdateCommentsException(content, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void updateCommentsThrowsIllegalArgumentException() throws Exception {
        when(commentsService.update(anyLong(), anyString()))
                .thenThrow(new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage()));

        ResultMatcher resultMatcher = createResultMatcher();
        testUpdateCommentsException(TEN_CHAR_STRING, times(1), resultMatcher);
    }

    @Test
    @WithMockUser
    public void deleteCommentsThrowsIllegalArgumentException() throws Exception {
        // given
        doThrow(new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage()))
                .when(commentsService).delete(anyLong());

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/{id}", ID);
        ResultMatcher resultMatcher = createResultMatcher();
        performAndExpectBadRequest(requestBuilder, null, resultMatcher);

        verify(commentsService).delete(eq(ID));
    }

    @Test
    @WithMockUser
    public void deleteSelectedCommentsThrowsIllegalArgumentException() throws Exception {
        // given
        List<Long> requestDto = Collections.emptyList();

        doThrow(new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage()))
                .when(commentsService).deleteSelectedComments(anyList());

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/selected-delete");
        ResultMatcher resultMatcher = createResultMatcher();
        performAndExpectBadRequest(requestBuilder, requestDto, resultMatcher);

        verify(commentsService).deleteSelectedComments(eq(requestDto));
    }

    private void testSaveCommentsException(String content, VerificationMode mode,
                                           ResultMatcher... resultMatchers) throws Exception {
        // given
        CommentsSaveDto requestDto = new CommentsSaveDto(content, ID);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING);
        performAndExpectBadRequest(requestBuilder, requestDto, resultMatchers);

        verify(commentsService, mode).save(any(CommentsSaveDto.class), any(Principal.class));
    }

    private void testUpdateCommentsException(String content, VerificationMode mode,
                                             ResultMatcher... resultMatchers) throws Exception {
        // given
        CommentsUpdateDto requestDto = new CommentsUpdateDto(content);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = put(REQUEST_MAPPING + "/{id}", ID);
        performAndExpectBadRequest(requestBuilder, requestDto, resultMatchers);

        verify(commentsService, mode).update(eq(ID), eq(requestDto.getContent()));
    }

    private ResultMatcher[] createResultMatchers(String rejectedValue, ExceptionMessages exceptionMessage) {
        return mockMvcTestHelper.createResultMatchers(CONTENT, rejectedValue, exceptionMessage);
    }

    private ResultMatcher createResultMatcher() {
        return mockMvcTestHelper.createResultMatcher(ILLEGAL_ARGUMENT_EXCEPTION_TEST);
    }

    private <T> void performAndExpectBadRequest(MockHttpServletRequestBuilder requestBuilder, T requestDto,
                                                ResultMatcher... resultMatchers) throws Exception {
        mockMvcTestHelper.performAndExpectBadRequest(requestBuilder, requestDto, resultMatchers);
    }
}
