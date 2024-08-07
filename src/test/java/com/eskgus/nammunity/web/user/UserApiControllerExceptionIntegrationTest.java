package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.enums.Fields;
import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.user.EmailUpdateDto;
import com.eskgus.nammunity.web.dto.user.NicknameUpdateDto;
import com.eskgus.nammunity.web.dto.user.PasswordUpdateDto;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static com.eskgus.nammunity.domain.enums.Fields.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserApiControllerExceptionIntegrationTest {
    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @Autowired
    private UserRepository userRepository;

    private User user;

    private static final String USERNAME_VALUE = USERNAME.getKey();
    private static final String PASSWORD_VALUE = PASSWORD.getKey();
    private static final String NICKNAME_VALUE = NICKNAME.getKey();
    private static final String EMAIL_VALUE = EMAIL.getKey();

    private static final String REQUEST_MAPPING = "/api/users";

    @BeforeEach
    public void setUp() {
        this.user = saveUser(1L);
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    @WithAnonymousUser
    public void signUpWithEmptyUsername() throws Exception {
        testSignUpException(USERNAME, "", EMPTY_USERNAME);
    }

    @Test
    @WithAnonymousUser
    public void signUpWithInvalidUsername() throws Exception {
        testSignUpException(USERNAME, USERNAME_VALUE, INVALID_USERNAME);
    }

    @Test
    @WithAnonymousUser
    public void signUpWithEmptyPassword() throws Exception {
        testSignUpException(PASSWORD, "", EMPTY_PASSWORD);
    }

    @Test
    @WithAnonymousUser
    public void signUpWithInvalidPassword() throws Exception {
        testSignUpException(PASSWORD, PASSWORD_VALUE, INVALID_PASSWORD);
    }

    @Test
    @WithAnonymousUser
    public void signUpWithEmptyConfirmPassword() throws Exception {
        testSignUpException(CONFIRM_PASSWORD, "", EMPTY_CONFIRM_PASSWORD);
    }

    @Test
    @WithAnonymousUser
    public void signUpWithEmptyNickname() throws Exception {
        testSignUpException(NICKNAME, "", EMPTY_NICKNAME);
    }

    @Test
    @WithAnonymousUser
    public void signUpWithInvalidNickname() throws Exception {
        testSignUpException(NICKNAME, NICKNAME_VALUE + "!", INVALID_NICKNAME);
    }

    @Test
    @WithAnonymousUser
    public void signUpWithEmptyEmail() throws Exception {
        testSignUpException(EMAIL, "", EMPTY_EMAIL);
    }

    @Test
    @WithAnonymousUser
    public void signUpWithInvalidEmail() throws Exception {
        testSignUpException(EMAIL, EMAIL_VALUE, INVALID_EMAIL);
    }

    @Test
    @WithAnonymousUser
    public void signUpWithExistentUsername() throws Exception {
        testSignUpException(USERNAME, user.getUsername(), USERNAME_EXISTS);
    }

    @Test
    @WithAnonymousUser
    public void signUpWithMismatchConfirmPassword() throws Exception {
        testSignUpException(CONFIRM_PASSWORD, PASSWORD_VALUE + user.getId(), CONFIRM_PASSWORD_MISMATCH);
    }

    @Test
    @WithAnonymousUser
    public void signUpWithExistentNickname() throws Exception {
        testSignUpException(NICKNAME, user.getNickname(), NICKNAME_EXISTS);
    }

    @Test
    @WithAnonymousUser
    public void signUpWithExistentEmail() throws Exception {
        testSignUpException(EMAIL, user.getEmail(), EMAIL_EXISTS);
    }

    @Test
    @WithAnonymousUser
    public void checkWithEmptyUsername() throws Exception {
        testCheckException(USERNAME, "", EMPTY_USERNAME);
    }

    @Test
    @WithAnonymousUser
    public void checkWithExistentUsername() throws Exception {
        testCheckException(USERNAME, user.getUsername(), USERNAME_EXISTS);
    }

    @Test
    @WithAnonymousUser
    public void checkWithEmptyNickname() throws Exception {
        testCheckException(NICKNAME, "", EMPTY_NICKNAME);
    }

    @Test
    @WithAnonymousUser
    public void checkWithExistentNickname() throws Exception {
        testCheckException(NICKNAME, user.getNickname(), NICKNAME_EXISTS);
    }

    @Test
    @WithAnonymousUser
    public void checkWithEmptyEmail() throws Exception {
        testCheckException(EMAIL, "", EMPTY_EMAIL);
    }

    @Test
    @WithAnonymousUser
    public void checkWithExistentEmail() throws Exception {
        testCheckException(EMAIL, user.getEmail(), EMAIL_EXISTS);
    }

    @Test
    @WithAnonymousUser
    public void updatePasswordWithAnonymousUser() throws Exception {
        // given
        PasswordUpdateDto requestDto = createPasswordUpdateDto(null, null);

        // when/then
        testUpdateThrowsUnauthorized(PASSWORD, requestDto);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updatePasswordWithEmptyOldPassword() throws Exception {
        testUpdatePasswordThrowsValidException(OLD_PASSWORD, "", EMPTY_OLD_PASSWORD);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updatePasswordWithEmptyPassword() throws Exception {
        testUpdatePasswordThrowsValidException(PASSWORD, "", EMPTY_NEW_PASSWORD);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updatePasswordWithInvalidPassword() throws Exception {
        testUpdatePasswordThrowsValidException(PASSWORD, PASSWORD_VALUE, INVALID_PASSWORD);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updatePasswordWithEmptyConfirmPassword() throws Exception {
        testUpdatePasswordThrowsValidException(CONFIRM_PASSWORD, "", EMPTY_CONFIRM_PASSWORD);
    }

    @Test
    @WithMockUser(username = "username2")
    public void updatePasswordWithNonExistentUsername() throws Exception {
        // given
        PasswordUpdateDto requestDto = createPasswordUpdateDto(null, null);

        // when/then
        testUpdateThrowsIllegalArgumentException(requestDto, PASSWORD_VALUE);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updatePasswordWithMismatchOldPassword() throws Exception {
        testUpdatePasswordThrowsValidException(OLD_PASSWORD, PASSWORD_VALUE, OLD_PASSWORD_MISMATCH);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updatePasswordWithInvalidNewPassword() throws Exception {
        testUpdatePasswordThrowsValidException(PASSWORD, PASSWORD_VALUE + user.getId(), INVALID_NEW_PASSWORD);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updatePasswordWithMismatchConfirmPassword() throws Exception {
        testUpdatePasswordThrowsValidException(CONFIRM_PASSWORD, PASSWORD_VALUE, CONFIRM_PASSWORD_MISMATCH);
    }

    @Test
    @WithAnonymousUser
    public void updateNicknameWithAnonymousUser() throws Exception {
        // given
        NicknameUpdateDto requestDto = createNicknameUpdateDto(NICKNAME_VALUE);

        // when/then
        testUpdateThrowsUnauthorized(NICKNAME, requestDto);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updateNicknameWithEmptyNickname() throws Exception {
        testUpdateNicknameThrowsValidException("", EMPTY_NICKNAME);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updateNicknameWithInvalidNickname() throws Exception {
        testUpdateNicknameThrowsValidException(NICKNAME_VALUE + "!", INVALID_NICKNAME);
    }

    @Test
    @WithMockUser(username = "username2")
    public void updateNicknameWithNonExistentUsername() throws Exception {
        // given
        NicknameUpdateDto requestDto = createNicknameUpdateDto(NICKNAME_VALUE);

        // when/then
        testUpdateThrowsIllegalArgumentException(requestDto, NICKNAME_VALUE);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updateNicknameWithInvalidNewNickname() throws Exception {
        testUpdateNicknameThrowsValidException(user.getNickname(), INVALID_NEW_NICKNAME);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updateNicknameWithExistentNickname() throws Exception {
        User user2 = saveUser(user.getId() + 1);

        testUpdateNicknameThrowsValidException(user2.getNickname(), NICKNAME_EXISTS);
    }

    @Test
    @WithAnonymousUser
    public void updateEmailWithAnonymousUser() throws Exception {
        // given
        EmailUpdateDto requestDto = createEmailUpdateDto(EMAIL_VALUE + "@naver.com");

        // when/then
        testUpdateThrowsUnauthorized(EMAIL, requestDto);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updateEmailWithEmptyEmail() throws Exception {
        testUpdateEmailThrowsValidException("", EMPTY_EMAIL);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updateEmailWithInvalidEmail() throws Exception {
        testUpdateEmailThrowsValidException(EMAIL_VALUE, INVALID_EMAIL);
    }

    @Test
    @WithMockUser(username = "username2")
    public void updateEmailWithNonExistentUsername() throws Exception {
        // given
        EmailUpdateDto requestDto = createEmailUpdateDto(EMAIL_VALUE + "@naver.com");

        // when/then
        testUpdateThrowsIllegalArgumentException(requestDto, EMAIL_VALUE);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updateEmailWithInvalidNewEmail() throws Exception {
        testUpdateEmailThrowsValidException(user.getEmail(), INVALID_NEW_EMAIL);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updateEmailWithExistentEmail() throws Exception {
        User user2 = saveUser(user.getId() + 1);

        testUpdateEmailThrowsValidException(user2.getEmail(), EMAIL_EXISTS);
    }

    @Test
    @WithAnonymousUser
    public void deleteUserWithAnonymousUser() throws Exception {
        // given
        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING);
        performAndExpectUnauthorized(requestBuilder, null);
    }

    @Test
    @WithMockUser(username = "username2")
    public void deleteUserWithNonExistentUsername() throws Exception {
        // given
        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING);
        ResultMatcher resultMatcher = createResultMatcher();
        performAndExpectBadRequest(requestBuilder, null, resultMatcher);
    }

    private RegistrationDto createRegistrationDto(Fields field, String value) {
        long id = user.getId() + 1;
        String username = USERNAME_VALUE + id;
        String password = PASSWORD_VALUE + id;
        String confirmPassword = password;
        String nickname = NICKNAME_VALUE + id;
        String email = EMAIL_VALUE + id + "@naver.com";

        switch (field) {
            case USERNAME -> username = value;
            case PASSWORD -> password = value;
            case CONFIRM_PASSWORD -> confirmPassword = value;
            case NICKNAME -> nickname = value;
            case EMAIL -> email = value;
        }

        return RegistrationDto.builder()
                .username(username).password(password).confirmPassword(confirmPassword)
                .nickname(nickname).email(email).build();
    }

    private PasswordUpdateDto createPasswordUpdateDto(Fields field, String value) {
        long id = user.getId();
        String oldPassword = PASSWORD_VALUE + id;
        String password = PASSWORD_VALUE + id + id;
        String confirmPassword = password;

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

    private NicknameUpdateDto createNicknameUpdateDto(String value) {
        return new NicknameUpdateDto(value);
    }

    private EmailUpdateDto createEmailUpdateDto(String value) {
        return new EmailUpdateDto(value);
    }

    private User saveUser(Long id) {
        Long userId = testDataHelper.signUp(id, Role.USER);
        return testDataHelper.assertOptionalAndGetEntity(userRepository::findById, userId);
    }

    private void testSignUpException(Fields field, String value, ExceptionMessages exceptionMessage) throws Exception {
        // given
        RegistrationDto requestDto = createRegistrationDto(field, value);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING);
        ResultMatcher[] resultMatchers = createResultMatchers(field, value, exceptionMessage);
        performAndExpectBadRequest(requestBuilder, requestDto, resultMatchers);
    }

    private void testCheckException(Fields field, String value, ExceptionMessages exceptionMessage) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get(REQUEST_MAPPING + "/validation");
        ResultMatcher[] resultMatchers = createResultMatchers(field, value, exceptionMessage);
        mockMvcTestHelper.performAndExpectBadRequestWithParam(requestBuilder, field, value, resultMatchers);
    }

    private <Dto> void testUpdateThrowsUnauthorized(Fields endpoint, Dto requestDto) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = put(REQUEST_MAPPING + "/" + endpoint.getKey());
        performAndExpectUnauthorized(requestBuilder, requestDto);
    }

    private void testUpdatePasswordThrowsValidException(Fields field, String value, ExceptionMessages exceptionMessage) throws Exception {
        // given
        PasswordUpdateDto requestDto = createPasswordUpdateDto(field, value);

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(field, value, exceptionMessage);
        testUpdateException(PASSWORD_VALUE, requestDto, resultMatchers);
    }

    private void testUpdateNicknameThrowsValidException(String value, ExceptionMessages exceptionMessage) throws Exception {
        // given
        NicknameUpdateDto requestDto = createNicknameUpdateDto(value);

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(NICKNAME, value, exceptionMessage);
        testUpdateException(NICKNAME_VALUE, requestDto, resultMatchers);
    }

    private void testUpdateEmailThrowsValidException(String value, ExceptionMessages exceptionMessage) throws Exception {
        // given
        EmailUpdateDto requestDto = createEmailUpdateDto(value);

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(EMAIL, value, exceptionMessage);
        testUpdateException(EMAIL_VALUE, requestDto, resultMatchers);
    }

    private <Dto> void testUpdateThrowsIllegalArgumentException(Dto requestDto, String endpoint) throws Exception {
        ResultMatcher resultMatcher = createResultMatcher();
        testUpdateException(endpoint, requestDto, resultMatcher);
    }

    private <Dto> void testUpdateException(String endpoint, Dto requestDto, ResultMatcher... resultMatchers) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = put(REQUEST_MAPPING + "/" + endpoint);
        performAndExpectBadRequest(requestBuilder, requestDto, resultMatchers);
    }

    private ResultMatcher[] createResultMatchers(Fields field, String rejectedValue, ExceptionMessages exceptionMessage) {
        return mockMvcTestHelper.createResultMatchers(field, rejectedValue, exceptionMessage);
    }

    private ResultMatcher createResultMatcher() {
        return mockMvcTestHelper.createResultMatcher(USERNAME_NOT_FOUND);
    }

    private <Dto> void performAndExpectUnauthorized(MockHttpServletRequestBuilder requestBuilder, Dto requestDto) throws Exception {
        mockMvcTestHelper.performAndExpectUnauthorized(requestBuilder, requestDto);
    }

    private <Dto> void performAndExpectBadRequest(MockHttpServletRequestBuilder requestBuilder, Dto requestDto,
                                                  ResultMatcher... resultMatchers) throws Exception {
        mockMvcTestHelper.performAndExpectBadRequest(requestBuilder, requestDto, resultMatchers);
    }
}
