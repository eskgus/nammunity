package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.config.WebConfig;
import com.eskgus.nammunity.config.interceptor.CommentsAuthInterceptor;
import com.eskgus.nammunity.config.interceptor.PostsAuthInterceptor;
import com.eskgus.nammunity.handler.CustomControllerAdvice;
import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.service.user.RegistrationService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.controller.api.user.ConfirmationApiController;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ConfirmationApiController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CustomControllerAdvice.class))
@Import(MockMvcTestHelper.class)
@MockBeans({ @MockBean(WebConfig.class), @MockBean(PostsAuthInterceptor.class), @MockBean(CommentsAuthInterceptor.class) })
public class ConfirmationApiControllerUnitTest {
    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private UserService userService;

    private static final Long ID = 1L;

    private static final String REQUEST_MAPPING = "/api/users";

    @Test
    @WithMockUser
    public void confirmToken() throws Exception {
        // given
        String token = "token";

        doNothing().when(registrationService).confirmToken(anyString());

        // when/then
        MockHttpServletRequestBuilder requestBuilder = get(REQUEST_MAPPING + "/confirm");
        ResultMatcher resultMatcher = flash().attributeCount(0);
        mockMvcTestHelper.requestAndAssertStatusIsFound(requestBuilder, token, resultMatcher);

        verify(registrationService).confirmToken(eq(token));
    }

    @Test
    @WithMockUser
    public void checkUserEnabled() throws Exception {
        // given
        String referer = "referer";
        String redirectUrl = "redirectUrl";

        when(registrationService.checkUserEnabled(anyLong(), anyString()))
                .thenReturn(redirectUrl);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = get(REQUEST_MAPPING + "/{id}/confirm", ID);
        ResultMatcher resultMatcher = mockMvcTestHelper.createResultMatcher(redirectUrl);
        mockMvcTestHelper.requestAndAssertStatusIsOkWithReferer(requestBuilder, referer, resultMatcher);

        verify(registrationService).checkUserEnabled(eq(ID), eq(referer));
    }

    @Test
    @WithMockUser
    public void resendToken() throws Exception {
        // given
        doNothing().when(registrationService).resendToken(anyLong());

        // when/then
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING + "/confirm");
        mockMvcTestHelper.requestAndAssertStatusIsOk(requestBuilder, ID);

        verify(registrationService).resendToken(eq(ID));
        verify(userService, never()).delete(anyLong());
    }
}
