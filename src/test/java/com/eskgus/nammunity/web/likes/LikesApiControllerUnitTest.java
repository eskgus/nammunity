package com.eskgus.nammunity.web.likes;

import com.eskgus.nammunity.config.WebConfig;
import com.eskgus.nammunity.config.interceptor.CommentsAuthInterceptor;
import com.eskgus.nammunity.config.interceptor.PostsAuthInterceptor;
import com.eskgus.nammunity.domain.enums.Fields;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import static com.eskgus.nammunity.domain.enums.Fields.COMMENTS_ID;
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
public class LikesApiControllerUnitTest {
    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @MockBean
    private LikesService likesService;

    private static final Long ID = 1L;

    private static final String REQUEST_MAPPING = "/api/likes";

    @Test
    @WithMockUser
    public void savePostLikes() throws Exception {
        testSaveLikes(POSTS_ID);

        verify(likesService).save(eq(ID), isNull(), any(Principal.class));
    }

    @Test
    @WithMockUser
    public void saveCommentLikes() throws Exception {
        testSaveLikes(COMMENTS_ID);

        verify(likesService).save(isNull(), eq(ID), any(Principal.class));
    }

    @Test
    @WithMockUser
    public void deletePostLikes() throws Exception {
        testDeleteLikes(POSTS_ID);

        verify(likesService).deleteByContentId(eq(ID), isNull(), any(Principal.class));
    }

    @Test
    @WithMockUser
    public void deleteCommentLikes() throws Exception {
        testDeleteLikes(COMMENTS_ID);

        verify(likesService).deleteByContentId(isNull(), eq(ID), any(Principal.class));
    }

    @Test
    @WithMockUser
    public void deleteSelectedLikes() throws Exception {
        // given
        List<Long> requestDto = Arrays.asList(ID, ID + 1, ID + 2);

        doNothing().when(likesService).deleteSelectedLikes(anyList());

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/selected-delete");
        mockMvcTestHelper.performAndExpectOk(requestBuilder, requestDto);

        verify(likesService).deleteSelectedLikes(eq(requestDto));
    }

    private void testSaveLikes(Fields field) throws Exception {
        // given
        when(likesService.save(anyLong(), anyLong(), any(Principal.class))).thenReturn(ID);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING);
        performAndExpectOkWithParam(requestBuilder, field);
    }

    private void testDeleteLikes(Fields field) throws Exception {
        // given
        doNothing().when(likesService).deleteByContentId(anyLong(), anyLong(), any(Principal.class));

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING);
        performAndExpectOkWithParam(requestBuilder, field);
    }

    private void performAndExpectOkWithParam(MockHttpServletRequestBuilder requestBuilder, Fields field) throws Exception {
        mockMvcTestHelper.performAndExpectOkWithParam(requestBuilder, field, ID);
    }
}
