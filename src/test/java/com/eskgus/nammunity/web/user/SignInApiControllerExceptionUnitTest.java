package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.config.WebConfig;
import com.eskgus.nammunity.config.interceptor.CommentsAuthInterceptor;
import com.eskgus.nammunity.config.interceptor.PostsAuthInterceptor;
import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.enums.Fields;
import com.eskgus.nammunity.handler.CustomControllerAdvice;
import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.service.user.SignInService;
import com.eskgus.nammunity.web.controller.api.user.SignInApiController;
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

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static com.eskgus.nammunity.domain.enums.Fields.USERNAME;
import static com.eskgus.nammunity.domain.enums.Fields.EMAIL;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = SignInApiController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CustomControllerAdvice.class))
@Import(MockMvcTestHelper.class)
@MockBeans({ @MockBean(WebConfig.class), @MockBean(PostsAuthInterceptor.class), @MockBean(CommentsAuthInterceptor.class) })
public class SignInApiControllerExceptionUnitTest {
    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @MockBean
    private SignInService signInService;

    private static final String REQUEST_MAPPING = "/api/users/sign-in";

    @Test
    @WithMockUser
    public void findUsernameWithEmptyEmail() throws Exception {
        testFindUsernameException("", EMPTY_EMAIL);
    }

    @Test
    @WithMockUser
    public void findUsernameWithInvalidEmail() throws Exception {
        testFindUsernameException(EMAIL.getKey(), INVALID_EMAIL);
    }

    @Test
    @WithMockUser
    public void findUsernameThrowsIllegalArgumentException() throws Exception {
        when(signInService.findUsername(anyString()))
                .thenThrow(new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage()));

        testFindUsernameException(EMAIL.getKey() + "@naver.com", ILLEGAL_ARGUMENT_EXCEPTION_TEST);
    }

    @Test
    @WithMockUser
    public void findPasswordWithEmptyUsername() throws Exception {
        testFindPasswordException("", EMPTY_USERNAME);
    }

    @Test
    @WithMockUser
    public void findPasswordThrowsIllegalArgumentException() throws Exception {
        doThrow(new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage()))
                .when(signInService).findPassword(anyString());

        testFindPasswordException(USERNAME.getKey(), ILLEGAL_ARGUMENT_EXCEPTION_TEST);
    }

    private void testFindUsernameException(String email, ExceptionMessages exceptionMessage) throws Exception {
        // given
        // when/then
        MockHttpServletRequestBuilder requestBuilder = get(REQUEST_MAPPING + "/username");
        performAndExpectBadRequestWithParam(requestBuilder, EMAIL, email, exceptionMessage);
    }

    private void testFindPasswordException(String username, ExceptionMessages exceptionMessage) throws Exception {
        // given
        // when/then
        MockHttpServletRequestBuilder requestBuilder = put(REQUEST_MAPPING + "/password");
        performAndExpectBadRequestWithParam(requestBuilder, USERNAME, username, exceptionMessage);
    }

    private void performAndExpectBadRequestWithParam(MockHttpServletRequestBuilder requestBuilder, Fields field,
                                                     String value, ExceptionMessages exceptionMessage) throws Exception {
        ResultMatcher resultMatcher = mockMvcTestHelper.createResultMatcher(exceptionMessage);
        mockMvcTestHelper.performAndExpectBadRequestWithParam(requestBuilder, field, value, resultMatcher);
    }
}
