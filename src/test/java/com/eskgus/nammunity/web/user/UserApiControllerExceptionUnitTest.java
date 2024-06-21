package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.config.WebConfig;
import com.eskgus.nammunity.config.interceptor.CommentsAuthInterceptor;
import com.eskgus.nammunity.config.interceptor.PostsAuthInterceptor;
import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.exception.CustomValidException;
import com.eskgus.nammunity.exception.SocialException;
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

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UserApiController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CustomControllerAdvice.class))
@Import(MockMvcTestHelper.class)
@MockBeans({ @MockBean(WebConfig.class), @MockBean(PostsAuthInterceptor.class), @MockBean(CommentsAuthInterceptor.class) })
public class UserApiControllerExceptionUnitTest {
    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private UserUpdateService userUpdateService;

    private static final Long ID = 1L;
    private static final String USERNAME = "username";
    private static final String USERNAME_VALUE = USERNAME + ID;
    private static final String OLD_PASSWORD = "oldPassword";
    private static final String PASSWORD = "password";
    private static final String CONFIRM_PASSWORD = "confirmPassword";
    private static final String PASSWORD_VALUE = PASSWORD + ID;
    private static final String NICKNAME = "nickname";
    private static final String EMAIL = "email";
    private static final String EMAIL_VALUE = EMAIL + "@naver.com";
    private static final String SOCIAL = "social";

    private static final String REQUEST_MAPPING = "/api/users";

    @Test
    @WithMockUser
    public void signUpWithEmptyUsername() throws Exception {
        // give
        RegistrationDto requestDto = createRegistrationDto(USERNAME, "");

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(
                USERNAME, EMPTY_USERNAME.getMessage(), requestDto.getUsername());
        testSignUpException(requestDto, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void signUpWithInvalidUsername() throws Exception {
        // give
        RegistrationDto requestDto = createRegistrationDto(USERNAME, USERNAME);

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(
                USERNAME, INVALID_USERNAME.getMessage(), requestDto.getUsername());
        testSignUpException(requestDto, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void signUpWithEmptyPassword() throws Exception {
        // give
        RegistrationDto requestDto = createRegistrationDto(PASSWORD, "");

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(
                PASSWORD, EMPTY_PASSWORD.getMessage(), requestDto.getPassword());
        testSignUpException(requestDto, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void signUpWithInvalidPassword() throws Exception {
        // give
        RegistrationDto requestDto = createRegistrationDto(PASSWORD, PASSWORD);

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(
                PASSWORD, INVALID_PASSWORD.getMessage(), requestDto.getPassword());
        testSignUpException(requestDto, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void signUpWithEmptyConfirmPassword() throws Exception {
        // give
        RegistrationDto requestDto = createRegistrationDto(CONFIRM_PASSWORD, "");

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(
                CONFIRM_PASSWORD, EMPTY_CONFIRM_PASSWORD.getMessage(), requestDto.getConfirmPassword());
        testSignUpException(requestDto, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void signUpWithEmptyNickname() throws Exception {
        // give
        RegistrationDto requestDto = createRegistrationDto(NICKNAME, "");

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(
                NICKNAME, EMPTY_NICKNAME.getMessage(), requestDto.getNickname());
        testSignUpException(requestDto, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void signUpWithInvalidNickname() throws Exception {
        // give
        RegistrationDto requestDto = createRegistrationDto(NICKNAME, NICKNAME + "!");

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(
                NICKNAME, INVALID_NICKNAME.getMessage(), requestDto.getNickname());
        testSignUpException(requestDto, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void signUpWithEmptyEmail() throws Exception {
        // give
        RegistrationDto requestDto = createRegistrationDto(EMAIL, "");

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(EMAIL, EMPTY_EMAIL.getMessage(), requestDto.getEmail());
        testSignUpException(requestDto, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void signUpWithInvalidEmail() throws Exception {
        // give
        RegistrationDto requestDto = createRegistrationDto(EMAIL, EMAIL);

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(EMAIL, INVALID_EMAIL.getMessage(), requestDto.getEmail());
        testSignUpException(requestDto, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void signUpThrowsCustomValidException() throws Exception {
        // given
        RegistrationDto requestDto = createRegistrationDto(null, null);

        when(registrationService.signUp(any(RegistrationDto.class)))
                .thenThrow(new CustomValidException(
                        USERNAME, requestDto.getUsername(), CUSTOM_VALID_EXCEPTION_TEST.getMessage()));

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(
                USERNAME, CUSTOM_VALID_EXCEPTION_TEST.getMessage(), requestDto.getUsername());
        testSignUpException(requestDto, times(1), resultMatchers);
    }

    @Test
    @WithMockUser
    public void signUpThrowsIllegalArgumentException() throws Exception {
        // given
        RegistrationDto requestDto = createRegistrationDto(null, null);

        when(registrationService.signUp(any(RegistrationDto.class)))
                .thenThrow(new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage()));

        // when/then
        ResultMatcher resultMatcher = createResultMatcher(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage());
        testSignUpException(requestDto, times(1), resultMatcher);
    }

    @Test
    @WithMockUser
    public void checkThrowsCustomValidException() throws Exception {
        // given
        when(registrationService.check(anyString(), isNull(), isNull()))
                .thenThrow(new CustomValidException(USERNAME, USERNAME_VALUE, CUSTOM_VALID_EXCEPTION_TEST.getMessage()));

        // when/then
        MockHttpServletRequestBuilder requestBuilder = get(REQUEST_MAPPING + "/validation");
        ResultMatcher[] resultMatchers = createResultMatchers(
                USERNAME, CUSTOM_VALID_EXCEPTION_TEST.getMessage(), USERNAME_VALUE);
        mockMvcTestHelper.requestAndAssertStatusIsBadRequestWithParam(
                requestBuilder, USERNAME, USERNAME_VALUE, resultMatchers);

        verify(registrationService).check(eq(USERNAME_VALUE), isNull(), isNull());
    }

    @Test
    @WithMockUser
    public void updatePasswordWithEmptyOldPassword() throws Exception {
        // given
        PasswordUpdateDto requestDto = createPasswordUpdateDto(OLD_PASSWORD, "");

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(
                OLD_PASSWORD, EMPTY_OLD_PASSWORD.getMessage(), requestDto.getOldPassword());
        testUpdatePasswordException(requestDto, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void updatePasswordWithEmptyPassword() throws Exception {
        // given
        PasswordUpdateDto requestDto = createPasswordUpdateDto(PASSWORD, "");

        // when/then
        ResultMatcher[] resultMatchers =
                createResultMatchers(PASSWORD, EMPTY_NEW_PASSWORD.getMessage(), requestDto.getPassword());
        testUpdatePasswordException(requestDto, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void updatePasswordWithInvalidPassword() throws Exception {
        // given
        PasswordUpdateDto requestDto = createPasswordUpdateDto(PASSWORD, PASSWORD);

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(
                PASSWORD, INVALID_PASSWORD.getMessage(), requestDto.getPassword());
        testUpdatePasswordException(requestDto, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void updatePasswordWithEmptyConfirmPassword() throws Exception {
        // given
        PasswordUpdateDto requestDto = createPasswordUpdateDto(CONFIRM_PASSWORD, "");

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(
                CONFIRM_PASSWORD, EMPTY_CONFIRM_PASSWORD.getMessage(), requestDto.getConfirmPassword());
        testUpdatePasswordException(requestDto, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void updatePasswordThrowsIllegalArgumentException() throws Exception {
        // given
        PasswordUpdateDto requestDto = createPasswordUpdateDto(null, null);

        when(userUpdateService.updatePassword(any(PasswordUpdateDto.class), any(Principal.class)))
                .thenThrow(new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage()));

        // when/then
        ResultMatcher resultMatcher = createResultMatcher(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage());
        testUpdatePasswordException(requestDto, times(1), resultMatcher);
    }

    @Test
    @WithMockUser
    public void updatePasswordThrowsCustomValidException() throws Exception {
        // given
        PasswordUpdateDto requestDto = createPasswordUpdateDto(null, null);

        when(userUpdateService.updatePassword(any(PasswordUpdateDto.class), any(Principal.class)))
                .thenThrow(new CustomValidException(
                        OLD_PASSWORD, CUSTOM_VALID_EXCEPTION_TEST.getMessage(), requestDto.getOldPassword()));

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(
                OLD_PASSWORD, requestDto.getOldPassword(), CUSTOM_VALID_EXCEPTION_TEST.getMessage());
        testUpdatePasswordException(requestDto, times(1), resultMatchers);
    }

    @Test
    @WithMockUser
    public void updateNicknameWithEmptyNickname() throws Exception {
        // given
        NicknameUpdateDto requestDto = createNicknameUpdateDto("");

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(
                NICKNAME, EMPTY_NICKNAME.getMessage(), requestDto.getNickname());
        testUpdateNicknameException(requestDto, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void updateNicknameWithInvalidNickname() throws Exception {
        // given
        NicknameUpdateDto requestDto = createNicknameUpdateDto(NICKNAME + "!");

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(
                NICKNAME, INVALID_NICKNAME.getMessage(), requestDto.getNickname());
        testUpdateNicknameException(requestDto, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void updateNicknameThrowsIllegalArgumentException() throws Exception {
        // given
        NicknameUpdateDto requestDto = createNicknameUpdateDto(NICKNAME);

        when(userUpdateService.updateNickname(any(NicknameUpdateDto.class), any(Principal.class)))
                .thenThrow(new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage()));

        // when/then
        ResultMatcher resultMatcher = createResultMatcher(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage());
        testUpdateNicknameException(requestDto, times(1), resultMatcher);
    }

    @Test
    @WithMockUser
    public void updateNicknameThrowsCustomValidException() throws Exception {
        // given
        NicknameUpdateDto requestDto = createNicknameUpdateDto(NICKNAME);

        when(userUpdateService.updateNickname(any(NicknameUpdateDto.class), any(Principal.class)))
                .thenThrow(new CustomValidException(
                        NICKNAME, requestDto.getNickname(), CUSTOM_VALID_EXCEPTION_TEST.getMessage()));

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(
                NICKNAME, CUSTOM_VALID_EXCEPTION_TEST.getMessage(), requestDto.getNickname());
        testUpdateNicknameException(requestDto, times(1), resultMatchers);
    }

    @Test
    @WithMockUser
    public void updateEmailWithEmptyEmail() throws Exception {
        // given
        EmailUpdateDto requestDto = createEmailUpdateDto("");

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(EMAIL, EMPTY_EMAIL.getMessage(), requestDto.getEmail());
        testUpdateEmailException(requestDto, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void updateEmailWithInvalidEmail() throws Exception {
        // given
        EmailUpdateDto requestDto = createEmailUpdateDto(EMAIL);

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(EMAIL, INVALID_EMAIL.getMessage(), requestDto.getEmail());
        testUpdateEmailException(requestDto, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void updateEmailThrowsIllegalArgumentException() throws Exception {
        // given
        EmailUpdateDto requestDto = createEmailUpdateDto(EMAIL_VALUE);

        when(userUpdateService.updateEmail(any(EmailUpdateDto.class), any(Principal.class)))
                .thenThrow(new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage()));

        // when/then
        ResultMatcher resultMatcher = createResultMatcher(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage());
        testUpdateEmailException(requestDto, times(1), resultMatcher);
    }

    @Test
    @WithMockUser
    public void updateEmailThrowsCustomValidException() throws Exception {
        // given
        EmailUpdateDto requestDto = createEmailUpdateDto(EMAIL_VALUE);

        when(userUpdateService.updateEmail(any(EmailUpdateDto.class), any(Principal.class)))
                .thenThrow(new CustomValidException(
                        EMAIL, requestDto.getEmail(), CUSTOM_VALID_EXCEPTION_TEST.getMessage()));

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(
                EMAIL, CUSTOM_VALID_EXCEPTION_TEST.getMessage(), requestDto.getEmail());
        testUpdateEmailException(requestDto, times(1), resultMatchers);
    }

    @Test
    @WithMockUser
    public void deleteUserThrowsIllegalArgumentException() throws Exception {
        // given
        when(userUpdateService.deleteUser(any(Principal.class), anyString()))
                .thenThrow(new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage()));

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING);
        String cookieValue = testDeleteUserOrUnlinkSocial(requestBuilder, ILLEGAL_ARGUMENT_EXCEPTION_TEST);

        verify(userUpdateService).deleteUser(any(Principal.class), eq(cookieValue));
    }

    @Test
    @WithMockUser
    public void unlinkSocialThrowsIllegalArgumentException() throws Exception {
        // given
        when(userUpdateService.unlinkSocial(any(Principal.class), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage()));

        // when/then
        testUnlinkSocialException(ILLEGAL_ARGUMENT_EXCEPTION_TEST);
    }

    @Test
    @WithMockUser
    public void unlinkSocialThrowsSocialException() throws Exception {
        // given
        when(userUpdateService.unlinkSocial(any(Principal.class), anyString(), anyString()))
                .thenThrow(new SocialException(USERNAME, SOCIAL, SOCIAL));

        // when/then
        testUnlinkSocialException(INTERNAL_SERVER_ERROR);
    }

    private RegistrationDto createRegistrationDto(String field, String value) {
        String username = USERNAME_VALUE;
        String password = PASSWORD_VALUE;
        String confirmPassword = PASSWORD_VALUE;
        String nickname = NICKNAME;
        String email = EMAIL_VALUE;

        if (field != null) {
            switch (field) {
                case USERNAME -> username = value;
                case PASSWORD -> password = value;
                case CONFIRM_PASSWORD -> confirmPassword = value;
                case NICKNAME -> nickname = value;
                case EMAIL -> email = value;
            }
        }

        return RegistrationDto.builder()
                .username(username).password(password).confirmPassword(confirmPassword)
                .nickname(nickname).email(email).build();
    }

    private PasswordUpdateDto createPasswordUpdateDto(String field, String value) {
        String oldPassword = PASSWORD_VALUE;
        String password = PASSWORD_VALUE;
        String confirmPassword = PASSWORD_VALUE;

        if (field != null) {
            switch (field) {
                case OLD_PASSWORD -> oldPassword = value;
                case PASSWORD -> password = value;
                case CONFIRM_PASSWORD -> confirmPassword = value;
            }
        }

        return PasswordUpdateDto.builder()
                .oldPassword(oldPassword).password(password).confirmPassword(confirmPassword).build();
    }

    private NicknameUpdateDto createNicknameUpdateDto(String nickname) {
        return new NicknameUpdateDto(nickname);
    }

    private EmailUpdateDto createEmailUpdateDto(String email) {
        return new EmailUpdateDto(email);
    }

    private Cookie createCookie() {
        return new Cookie("access_token", "accessToken");
    }

    private ResultMatcher[] createResultMatchers(String expectedField, String expectedDefaultMessage,
                                                 String expectedRejectedValue) {
        return mockMvcTestHelper.createResultMatchers(expectedField, expectedDefaultMessage, expectedRejectedValue);
    }

    private ResultMatcher createResultMatcher(String exceptionMessage) {
        return mockMvcTestHelper.createResultMatcher(exceptionMessage);
    }

    private void testSignUpException(RegistrationDto requestDto, VerificationMode mode,
                                     ResultMatcher... resultMatchers) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING);
        mockMvcTestHelper.requestAndAssertStatusIsBadRequest(requestBuilder, requestDto, resultMatchers);

        verify(registrationService, mode).signUp(any(RegistrationDto.class));
    }

    private void testUpdatePasswordException(PasswordUpdateDto requestDto, VerificationMode mode,
                                             ResultMatcher... resultMatchers) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = put(REQUEST_MAPPING + "/" + PASSWORD);
        mockMvcTestHelper.requestAndAssertStatusIsBadRequest(requestBuilder, requestDto, resultMatchers);

        verify(userUpdateService, mode).updatePassword(any(PasswordUpdateDto.class), any(Principal.class));
    }

    private void testUpdateNicknameException(NicknameUpdateDto requestDto, VerificationMode mode,
                                             ResultMatcher... resultMatchers) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = put(REQUEST_MAPPING + "/" + NICKNAME);
        mockMvcTestHelper.requestAndAssertStatusIsBadRequest(requestBuilder, requestDto, resultMatchers);

        verify(userUpdateService, mode).updateNickname(any(NicknameUpdateDto.class), any(Principal.class));
    }

    private void testUpdateEmailException(EmailUpdateDto requestDto, VerificationMode mode,
                                          ResultMatcher... resultMatchers) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = put(REQUEST_MAPPING + "/" + EMAIL);
        mockMvcTestHelper.requestAndAssertStatusIsBadRequest(requestBuilder, requestDto, resultMatchers);

        verify(userUpdateService, mode).updateEmail(any(EmailUpdateDto.class), any(Principal.class));
    }

    private void testUnlinkSocialException(ExceptionMessages exceptionMessage) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING + "/unlink/{social}", SOCIAL);
        String cookieValue = testDeleteUserOrUnlinkSocial(requestBuilder, exceptionMessage);

        verify(userUpdateService).unlinkSocial(any(Principal.class), eq(SOCIAL), eq(cookieValue));
    }

    private String testDeleteUserOrUnlinkSocial(MockHttpServletRequestBuilder requestBuilder,
                                              ExceptionMessages exceptionMessage) throws Exception {
        Cookie cookie = createCookie();

        ResultMatcher resultMatcher = createResultMatcher(exceptionMessage.getMessage());
        if (INTERNAL_SERVER_ERROR.equals(exceptionMessage)) {
            mockMvcTestHelper.requestAndAssertStatusIsInternalServerError(requestBuilder, cookie, resultMatcher);
        } else {
            mockMvcTestHelper.requestAndAssertStatusIsBadRequestWithCookie(requestBuilder, cookie, resultMatcher);
        }


        return cookie.getValue();
    }
}
