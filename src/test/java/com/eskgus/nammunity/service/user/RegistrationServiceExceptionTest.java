package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.enums.Fields;
import com.eskgus.nammunity.domain.tokens.Tokens;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.exception.CustomValidException;
import com.eskgus.nammunity.service.email.EmailService;
import com.eskgus.nammunity.service.tokens.TokensService;
import com.eskgus.nammunity.util.ServiceTestUtil;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static com.eskgus.nammunity.domain.enums.Fields.TOKEN;
import static com.eskgus.nammunity.domain.enums.Fields.USER_ID;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceExceptionTest {
    @Mock
    private UserService userService;

    @Mock
    private BCryptPasswordEncoder encoder;

    @Mock
    private EmailService emailService;

    @Mock
    private TokensService tokensService;

    @InjectMocks
    private RegistrationService registrationService;

    private static final Long ID = 1L;
    private static final Fields USERNAME = Fields.USERNAME;
    private static final String USERNAME_VALUE = USERNAME.getKey() + ID;
    private static final Fields PASSWORD = Fields.PASSWORD;
    private static final String PASSWORD_VALUE = PASSWORD.getKey() + ID;
    private static final Fields CONFIRM_PASSWORD = Fields.CONFIRM_PASSWORD;
    private static final Fields NICKNAME = Fields.NICKNAME;
    private static final String NICKNAME_VALUE = NICKNAME.getKey() + ID;
    private static final Fields EMAIL = Fields.EMAIL;
    private static final String EMAIL_VALUE = EMAIL.getKey() + ID + "@naver.com";

    @Test
    public void signUpWithExistentUsername() {
        testSignUpWithMatchedConfirmPassword(userService::existsByUsername, USERNAME, USERNAME_EXISTS);
    }

    @Test
    public void signUpWithMismatchConfirmPassword() {
        testSignUpThrowsCustomValidException(false, CONFIRM_PASSWORD, CONFIRM_PASSWORD_MISMATCH);
    }

    @Test
    public void signUpWithExistentNickname() {
        testSignUpWithMatchedConfirmPassword(userService::existsByNickname, NICKNAME, NICKNAME_EXISTS);
    }

    @Test
    public void signUpWithExistentEmail() {
        testSignUpWithMatchedConfirmPassword(userService::existsByEmail, EMAIL, EMAIL_EXISTS);
    }

    @Test
    public void signUpWithNonExistentUser() {
        // given
        RegistrationDto registrationDto = createRegistrationDto(true);

        giveChecker(userService::existsByUsername, false);
        giveChecker(userService::existsByNickname, false);
        giveChecker(userService::existsByEmail, false);

        when(userService.save(any(RegistrationDto.class))).thenReturn(ID);

        testIllegalArgumentException(
                userService::findById, USER_NOT_FOUND, () -> registrationService.signUp(registrationDto));

        verifySignUp(USER_ID, registrationDto);
    }

    @Test
    public void resendTokenWithNonExistentUser() {
        testIllegalArgumentException(userService::findById, USER_NOT_FOUND, () -> registrationService.resendToken(ID));

        verifyResendToken();
    }

    @Test
    public void resendTokenWithConfirmedEmail() {
        testResendTokenException(true, EMAIL_CONFIRMED);
    }

    @Test
    public void resendTokenThrowsResendNotAllowedException() {
        testResendTokenException(false, RESEND_NOT_ALLOWED);
    }

    @Test
    public void sendTokenWithNonExistentUser() {
        testIllegalArgumentException(
                userService::findById, USER_NOT_FOUND,
                () -> registrationService.sendToken(ID, EMAIL_VALUE, "purpose"));

        verify(userService).findById(eq(ID));
        verifySendToken();
    }

    @Test
    public void confirmTokenWithNonExistentToken() {
        testIllegalArgumentException(
                tokensService::findByToken, TOKEN_NOT_FOUND, () -> registrationService.confirmToken(TOKEN.getKey()));

        verifyConfirmToken();
    }

    @Test
    public void confirmTokenWithConfirmedEmail() {
        // given
        Tokens token = giveToken();

        when(token.getConfirmedAt()).thenReturn(LocalDateTime.now());

        // when/then
        testConfirmToken(EMAIL_CONFIRMED, token);
    }

    @Test
    public void confirmTokenWithExpiredToken() {
        // given
        Tokens token = giveToken();

        when(token.getExpiredAt()).thenReturn(LocalDateTime.now().minusMinutes(1));

        // when/then
        testConfirmToken(TOKEN_EXPIRED, token);
    }

    @Test
    public void checkUsernameWithEmptyUsername() {
        testCheckException(USERNAME, "", EMPTY_USERNAME);
    }

    @Test
    public void checkUsernameWithExistentUsername() {
        giveChecker(userService::existsByUsername, true);

        testCheckException(USERNAME, USERNAME_VALUE, USERNAME_EXISTS);
    }

    @Test
    public void checkNicknameWithEmptyNickname() {
        testCheckException(NICKNAME, "", EMPTY_NICKNAME);
    }

    @Test
    public void checkNicknameWithExistentNickname() {
        giveChecker(userService::existsByNickname, true);

        testCheckException(NICKNAME, NICKNAME_VALUE, NICKNAME_EXISTS);
    }

    @Test
    public void checkEmailWithEmptyEmail() {
        testCheckException(EMAIL, "", EMPTY_EMAIL);
    }

    @Test
    public void checkEmailWithExistentEmail() {
        giveChecker(userService::existsByEmail, true);

        testCheckException(EMAIL, EMAIL_VALUE, EMAIL_EXISTS);
    }

    @Test
    public void checkUserEnabledWithNonExistentUser() {
        testIllegalArgumentException(
                userService::findById, USER_NOT_FOUND, () -> registrationService.checkUserEnabled(ID, "referer"));

        verify(userService).findById(eq(ID));
    }

    @Test
    public void checkUserEnabledWithNotConfirmedEmail() {
        // given
        User user = giveUser();

        giveEnabled(user, false);

        // when/then
        assertIllegalArgumentException(
                () -> registrationService.checkUserEnabled(ID, "referer"), EMAIL_NOT_CONFIRMED);

        verify(userService).findById(eq(ID));
        verify(user).isEnabled();
    }

    private void testSignUpWithMatchedConfirmPassword(Function<String, Boolean> checker, Fields field,
                                                      ExceptionMessages exceptionMessage) {
        giveChecker(checker, true);

        testSignUpThrowsCustomValidException(true, field, exceptionMessage);
    }

    private void testSignUpThrowsCustomValidException(boolean isPasswordConfirmed, Fields field,
                                                      ExceptionMessages exceptionMessage) {
        // given
        RegistrationDto registrationDto = createRegistrationDto(isPasswordConfirmed);

        String rejectedValue = getRejectedValue(field, registrationDto);
        CustomValidException customValidException = createCustomValidException(field, rejectedValue, exceptionMessage);

        // when/then
        assertCustomValidException(() -> registrationService.signUp(registrationDto), customValidException);

        verifySignUp(field, registrationDto);
    }

    private void testResendTokenException(boolean isEnabled, ExceptionMessages exceptionMessage) {
        // given
        User user = giveUser();

        giveEnabled(user, isEnabled);

        if (!isEnabled) {
            when(user.getCreatedDate()).thenReturn(LocalDateTime.now().minusMinutes(13));
        }

        VerificationMode mode = isEnabled ? never() : times(1);

        // when/then
        assertIllegalArgumentException(() -> registrationService.resendToken(ID), exceptionMessage);

        verifyResendToken();
        verify(user).isEnabled();
        verify(user, mode).getCreatedDate();
        verify(user, never()).getTokens();
    }

    private void testConfirmToken(ExceptionMessages exceptionMessage, Tokens token) {
        VerificationMode expiredMode = TOKEN_EXPIRED.equals(exceptionMessage) ? times(1) : never();

        assertIllegalArgumentException(() -> registrationService.confirmToken(TOKEN.getKey()), exceptionMessage);

        verifyConfirmToken();
        verify(token).getConfirmedAt();
        verify(token, expiredMode).getExpiredAt();
        verify(token, never()).updateConfirmedAt(any(LocalDateTime.class));
        verify(token, never()).getUser();
    }

    private void testCheckException(Fields field, String rejectedValue, ExceptionMessages exceptionMessage) {
        // given
        Map<Fields, String> fieldValues = createFieldValues(field, rejectedValue);

        String username = giveFieldValue(fieldValues, USERNAME);
        String nickname = giveFieldValue(fieldValues, NICKNAME);
        String email = giveFieldValue(fieldValues, EMAIL);

        CustomValidException customValidException = createCustomValidException(field, rejectedValue, exceptionMessage);

        List<VerificationMode> modes = setCheckModes(field, rejectedValue);
        List<String> values = Arrays.asList(username, nickname, email);

        // when/then
        assertCustomValidException(() -> registrationService.check(username, nickname, email), customValidException);

        verifyCheck(modes, values);
    }

    private <Entity, ParamType> void testIllegalArgumentException(Function<ParamType, Entity> finder,
                                                                  ExceptionMessages exceptionMessage,
                                                                  Executable executable) {
        throwIllegalArgumentException(finder, exceptionMessage);

        assertIllegalArgumentException(executable, exceptionMessage);
    }

    private void giveChecker(Function<String, Boolean> checker, boolean exists) {
        ServiceTestUtil.giveChecker(checker, exists);
    }

    private RegistrationDto createRegistrationDto(boolean isPasswordConfirmed) {
        String password = PASSWORD_VALUE;
        String confirmPassword = isPasswordConfirmed ? password : CONFIRM_PASSWORD.getKey();

        return RegistrationDto.builder()
                .username(USERNAME_VALUE).password(password).confirmPassword(confirmPassword)
                .nickname(NICKNAME_VALUE).email(EMAIL_VALUE).build();
    }

    private String getRejectedValue(Fields field, RegistrationDto registrationDto) {
        return switch (field) {
            case USERNAME -> registrationDto.getUsername();
            case CONFIRM_PASSWORD -> registrationDto.getConfirmPassword();
            case NICKNAME -> registrationDto.getNickname();
            default -> registrationDto.getEmail();
        };
    }

    private CustomValidException createCustomValidException(Fields field, String rejectedValue,
                                                            ExceptionMessages exceptionMessage) {
        return ServiceTestUtil.createCustomValidException(field, rejectedValue, exceptionMessage);
    }

    private User giveUser() {
        return ServiceTestUtil.giveUser(userService::findById, Long.class);
    }

    private Tokens giveToken() {
        Tokens token = mock(Tokens.class);
        when(tokensService.findByToken(anyString())).thenReturn(token);

        return token;
    }

    private Map<Fields, String> createFieldValues(Fields field, String rejectedValue) {
        Map<Fields, String> fieldValues = new HashMap<>();
        fieldValues.put(field, rejectedValue);

        return fieldValues;
    }

    private String giveFieldValue(Map<Fields, String> fieldValues, Fields field) {
        return fieldValues.getOrDefault(field, null);
    }

    private void giveEnabled(User user, boolean isEnabled) {
        when(user.isEnabled()).thenReturn(isEnabled);
    }

    private <Entity, ParamType> void throwIllegalArgumentException(Function<ParamType, Entity> finder,
                                                                   ExceptionMessages exceptionMessage) {
        ServiceTestUtil.throwIllegalArgumentException(finder, exceptionMessage);
    }

    private void assertCustomValidException(Executable executable, CustomValidException customValidException) {
        ServiceTestUtil.assertCustomValidException(executable, customValidException);
    }

    private void assertIllegalArgumentException(Executable executable, ExceptionMessages exceptionMessage) {
        ServiceTestUtil.assertIllegalArgumentException(executable, exceptionMessage);
    }

    private void verifySignUp(Fields field, RegistrationDto registrationDto) {
        List<VerificationMode> signUpModes = setSignUpModes(field);

        List<VerificationMode> checkModes = Arrays.asList(
                times(1), signUpModes.get(0), signUpModes.get(1));
        List<String> values = Arrays.asList(
                registrationDto.getUsername(), registrationDto.getNickname(), registrationDto.getEmail());

        verifyCheck(checkModes, values);
        verify(userService, signUpModes.get(2)).save(any(RegistrationDto.class));
        verify(userService, signUpModes.get(2)).findById(eq(ID));
    }

    private void verifyResendToken() {
        verify(userService, times(1)).findById(eq(ID));
        verifySendToken();
    }

    private void verifySendToken() {
        verify(tokensService, never()).save(any(Tokens.class));
        verify(emailService, never()).setConfirmEmailText(anyString(), anyString());
        verify(emailService, never()).send(anyString(), anyString());
    }

    private void verifyConfirmToken() {
        verify(tokensService).findByToken(eq(TOKEN.getKey()));
    }

    private void verifyCheck(List<VerificationMode> modes, List<String> values) {
        verify(userService, modes.get(0)).existsByUsername(eq(values.get(0)));
        verify(userService, modes.get(1)).existsByNickname(eq(values.get(1)));
        verify(userService, modes.get(2)).existsByEmail(eq(values.get(2)));
    }

    private List<VerificationMode> setSignUpModes(Fields field) {
        List<VerificationMode> modes = new ArrayList<>(Collections.nCopies(3, never()));

        if (!USERNAME.equals(field) && !CONFIRM_PASSWORD.equals(field)) {
            modes.set(0, times(1));
        }
        if (EMAIL.equals(field) || USER_ID.equals(field)) {
            modes.set(1, times(1));
        }
        if (USER_ID.equals(field)) {
            modes.set(2, times(1));
        }

        return modes;
    }

    private List<VerificationMode> setCheckModes(Fields field, String rejectedValue) {
        List<VerificationMode> modes = new ArrayList<>(Collections.nCopies(3, never()));

        if (!rejectedValue.isBlank()) {
            switch (field) {
                case USERNAME -> modes.set(0, times(1));
                case NICKNAME -> modes.set(1, times(1));
                case EMAIL -> modes.set(2, times(1));
            }
        }

        return modes;
    }
}
