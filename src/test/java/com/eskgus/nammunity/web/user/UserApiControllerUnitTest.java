package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.config.WebConfig;
import com.eskgus.nammunity.config.interceptor.CommentsAuthInterceptor;
import com.eskgus.nammunity.config.interceptor.PostsAuthInterceptor;
import com.eskgus.nammunity.domain.enums.Fields;
import com.eskgus.nammunity.handler.CustomControllerAdvice;
import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.service.user.RegistrationService;
import com.eskgus.nammunity.service.user.UserUpdateService;
import com.eskgus.nammunity.web.controller.api.user.UserApiController;
import com.eskgus.nammunity.web.dto.user.EmailUpdateDto;
import com.eskgus.nammunity.web.dto.user.NicknameUpdateDto;
import com.eskgus.nammunity.web.dto.user.PasswordUpdateDto;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.security.Principal;

import static com.eskgus.nammunity.domain.enums.Fields.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UserApiController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CustomControllerAdvice.class))
@Import(MockMvcTestHelper.class)
@MockBeans({ @MockBean(WebConfig.class), @MockBean(PostsAuthInterceptor.class), @MockBean(CommentsAuthInterceptor.class) })
public class UserApiControllerUnitTest {
    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private UserUpdateService userUpdateService;

    private static final Long ID = 1L;
    private static final String USERNAME_VALUE = USERNAME.getKey() + ID;
    private static final String PASSWORD_VALUE = PASSWORD.getKey() + ID;
    private static final String NICKNAME_VALUE = NICKNAME.getKey();
    private static final String EMAIL_VALUE = EMAIL.getKey() + "@naver.com";

    private static final String REQUEST_MAPPING = "/api/users";

    @Test
    @WithMockUser
    public void signUp() throws Exception {
        // given
        RegistrationDto requestDto = RegistrationDto.builder()
                .username(USERNAME_VALUE).password(PASSWORD_VALUE).confirmPassword(PASSWORD_VALUE)
                .nickname(NICKNAME_VALUE).email(EMAIL_VALUE).build();

        when(registrationService.signUp(any(RegistrationDto.class))).thenReturn(ID);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING);
        performAndExpectOk(requestBuilder, requestDto);

        verify(registrationService).signUp(any(RegistrationDto.class));
    }

    @Test
    @WithMockUser
    public void checkUsername() throws Exception {
        // given
        when(registrationService.check(anyString(), isNull(), isNull())).thenReturn(true);

        // when/then
        testCheck(USERNAME, USERNAME_VALUE);

        verify(registrationService).check(eq(USERNAME_VALUE), isNull(), isNull());
    }

    @Test
    @WithMockUser
    public void checkNickname() throws Exception {
        // given
        when(registrationService.check(isNull(), anyString(), isNull())).thenReturn(true);

        // when/then
        testCheck(NICKNAME, NICKNAME_VALUE);

        verify(registrationService).check(isNull(), eq(NICKNAME_VALUE), isNull());
    }

    @Test
    @WithMockUser
    public void checkEmail() throws Exception {
        // given
        when(registrationService.check(isNull(), isNull(), anyString())).thenReturn(true);

        // when/then
        testCheck(EMAIL, EMAIL_VALUE);

        verify(registrationService).check(isNull(), isNull(), eq(EMAIL_VALUE));
    }

    @Test
    @WithMockUser
    public void updatePassword() throws Exception {
        // given
        PasswordUpdateDto requestDto = PasswordUpdateDto.builder()
                .oldPassword("old" + PASSWORD_VALUE).password(PASSWORD_VALUE).confirmPassword(PASSWORD_VALUE).build();

        when(userUpdateService.updatePassword(any(PasswordUpdateDto.class), any(Principal.class))).thenReturn(ID);

        // when/then
        testUpdate(PASSWORD, requestDto);

        verify(userUpdateService).updatePassword(any(PasswordUpdateDto.class), any(Principal.class));
    }

    @Test
    @WithMockUser
    public void updateNickname() throws Exception {
        // given
        NicknameUpdateDto requestDto = new NicknameUpdateDto(NICKNAME_VALUE);

        when(userUpdateService.updateNickname(any(NicknameUpdateDto.class), any(Principal.class))).thenReturn(ID);

        // when/then
        testUpdate(NICKNAME, requestDto);

        verify(userUpdateService).updateNickname(any(NicknameUpdateDto.class), any(Principal.class));
    }

    @Test
    @WithMockUser
    public void updateEmail() throws Exception {
        // given
        EmailUpdateDto requestDto = new EmailUpdateDto(EMAIL_VALUE);

        when(userUpdateService.updateEmail(any(EmailUpdateDto.class), any(Principal.class))).thenReturn(ID);

        // when/then
        testUpdate(EMAIL, requestDto);

        verify(userUpdateService).updateEmail(any(EmailUpdateDto.class), any(Principal.class));
    }

    @Test
    @WithMockUser
    public void deleteUserWithCookie() throws Exception {
        // given
        Pair<Cookie, ResponseCookie> pair = createCookies();

        HttpHeaders headers = createHeaders(pair.getSecond());

        when(userUpdateService.deleteUser(any(Principal.class), anyString())).thenReturn(headers);

        // when/then
        testDeleteUser(pair);

        verify(userUpdateService).deleteUser(any(Principal.class), eq(pair.getFirst().getValue()));
    }

    @Test
    @WithMockUser
    public void deleteUserWithoutCookie() throws Exception {
        // given
        when(userUpdateService.deleteUser(any(Principal.class), anyString())).thenReturn(null);

        // when/then
        testDeleteUser(null);

        verify(userUpdateService).deleteUser(any(Principal.class), isNull());
    }

    @Test
    @WithMockUser
    public void unlinkSocial() throws Exception {
        // given
        String social = "social";

        Pair<Cookie, ResponseCookie> pair = createCookies();

        HttpHeaders headers = createHeaders(pair.getSecond());

        when(userUpdateService.unlinkSocial(any(Principal.class), anyString(), anyString())).thenReturn(headers);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING + "/unlink/{social}", social);
        testDeleteUserOrUnlinkSocial(requestBuilder, pair);

        verify(userUpdateService).unlinkSocial(any(Principal.class), eq(social), eq(pair.getFirst().getValue()));
    }

    private void testCheck(Fields field, String value) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get(REQUEST_MAPPING + "/validation");
        mockMvcTestHelper.performAndExpectOkWithParam(requestBuilder, field, value);
    }

    private <T> void testUpdate(Fields endpoint, T requestDto) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = put(REQUEST_MAPPING + "/" + endpoint.getKey());
        performAndExpectOk(requestBuilder, requestDto);
    }

    private void testDeleteUser(Pair<Cookie, ResponseCookie> pair) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING);
        testDeleteUserOrUnlinkSocial(requestBuilder, pair);
    }

    private void testDeleteUserOrUnlinkSocial(MockHttpServletRequestBuilder requestBuilder,
                                              Pair<Cookie, ResponseCookie> pair) throws Exception {
        Cookie cookie = pair != null ? pair.getFirst() : null;

        ResultMatcher resultMatcher = cookie != null
                ? header().string(HttpHeaders.SET_COOKIE, pair.getSecond().toString())
                : header().doesNotExist(HttpHeaders.SET_COOKIE);
        mockMvcTestHelper.performAndExpectOkWithCookie(requestBuilder, cookie, resultMatcher);
    }

    private <T> void performAndExpectOk(MockHttpServletRequestBuilder requestBuilder, T requestDto) throws Exception {
        mockMvcTestHelper.performAndExpectOk(requestBuilder, requestDto);
    }

    private Pair<Cookie, ResponseCookie> createCookies() {
        Cookie cookie = new Cookie("access_token", "accessToken");

        ResponseCookie responseCookie = ResponseCookie.from(cookie.getName(), cookie.getValue()).build();

        return Pair.of(cookie, responseCookie);
    }

    private HttpHeaders createHeaders(ResponseCookie responseCookie) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, responseCookie.toString());

        return headers;
    }
}
