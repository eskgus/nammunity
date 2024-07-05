package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.enums.Fields;
import com.eskgus.nammunity.domain.tokens.Tokens;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.email.EmailService;
import com.eskgus.nammunity.service.tokens.TokensService;
import com.eskgus.nammunity.util.ServiceTestUtil;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

import static com.eskgus.nammunity.domain.enums.Fields.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {
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
    private static final Fields NICKNAME = Fields.NICKNAME;
    private static final String NICKNAME_VALUE = NICKNAME.getKey() + ID;
    private static final Fields EMAIL = Fields.EMAIL;
    private static final String EMAIL_VALUE = EMAIL.getKey() + ID + "@naver.com";
    private static final String TEXT = "text";
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Test
    public void signUp() {
        // given
        RegistrationDto registrationDto = createRegistrationDto();

        giveChecker(userService::existsByUsername);
        giveChecker(userService::existsByNickname);
        giveChecker(userService::existsByEmail);

        when(userService.save(any(RegistrationDto.class))).thenReturn(ID);

        boolean isRegistration = true;
        User user = giveSendToken(isRegistration);

        List<VerificationMode> modes = Collections.nCopies(3, times(1));
        List<String> values = Arrays.asList(USERNAME_VALUE, NICKNAME_VALUE, EMAIL_VALUE);

        // when
        Long result = registrationService.signUp(registrationDto);

        // then
        assertEquals(ID, result);

        verifyExists(modes, values);
        verify(userService).save(any(RegistrationDto.class));
        verifySendToken(times(1), user, isRegistration);
    }

    @Test
    public void resendToken() {
        // given
        boolean isRegistration = true;
        User user = giveSendToken(isRegistration);

        giveEnabled(user, false);

        when(user.getCreatedDate()).thenReturn(NOW);

        List<Tokens> tokens = giveTokens();
        when(user.getTokens()).thenReturn(tokens);

        for (Tokens token : tokens) {
            doNothing().when(token).updateExpiredAt(any(LocalDateTime.class));
        }

        ServiceTestUtil.giveEmail(user, EMAIL_VALUE);

        // when
        registrationService.resendToken(ID);

        // then
        verify(user).isEnabled();
        verify(user).getCreatedDate();
        verify(user).getTokens();
        tokens.forEach(token -> verify(token).updateExpiredAt(any(LocalDateTime.class)));
        verifySendToken(times(2), user, isRegistration);
    }

    @Test
    public void sendRegistrationToken() {
        testSendToken(true);
    }

    @Test
    public void sendUpdateToken() {
        testSendToken(false);
    }

    @Test
    public void encryptPassword() {
        // given
        String password = PASSWORD.getKey();
        String encryptPassword = PASSWORD_VALUE;

        when(encoder.encode(anyString())).thenReturn(encryptPassword);

        // when
        String result = registrationService.encryptPassword(password);

        // then
        assertEquals(encryptPassword, result);

        verify(encoder).encode(eq(password));
    }

    @Test
    public void confirmToken() {
        // given
        String tokenValue = TOKEN.getKey();

        Tokens token = mock(Tokens.class);
        when(tokensService.findByToken(anyString())).thenReturn(token);

        when(token.getExpiredAt()).thenReturn(NOW.plusDays(1));

        doNothing().when(token).updateConfirmedAt(any(LocalDateTime.class));

        User user = mock(User.class);
        when(token.getUser()).thenReturn(user);

        doNothing().when(user).updateEnabled();

        // when
        registrationService.confirmToken(tokenValue);

        // then
        verify(tokensService).findByToken(eq(tokenValue));
        verify(token).getConfirmedAt();
        verify(token).getExpiredAt();
        verify(token).updateConfirmedAt(any(LocalDateTime.class));
        verify(token).getUser();
        verify(user).updateEnabled();
    }

    @Test
    public void checkUsername() {
        testCheck(USERNAME, USERNAME_VALUE, userService::existsByUsername);
    }

    @Test
    public void checkNickname() {
        testCheck(NICKNAME, NICKNAME_VALUE, userService::existsByNickname);
    }

    @Test
    public void checkEmail() {
        testCheck(EMAIL, EMAIL_VALUE, userService::existsByEmail);
    }

    @Test
    public void checkUserEnabledInSignUp() {
        String signUp = "/users/sign-up";
        String signIn = "/users/sign-in";
        testCheckUserEnabled(signUp, signIn);
    }

    @Test
    public void checkUserEnabledInMyPage() {
        String myPage = "/users/my-page/update/user-info";
        testCheckUserEnabled(myPage, myPage);
    }

    private void testSendToken(boolean isRegistration) {
        // given
        User user = giveSendToken(isRegistration);

        String purpose = isRegistration ? "registration" : "update";

        // when
        registrationService.sendToken(ID, EMAIL_VALUE, purpose);

        // then
        verifySendToken(times(1), user, isRegistration);
    }

    private void testCheck(Fields field, String value, Function<String, Boolean> checker) {
        // given
        Map<Fields, String> fieldValues = createFieldValues(field, value);

        String username = giveFieldValue(fieldValues, USERNAME);
        String nickname = giveFieldValue(fieldValues, NICKNAME);
        String email = giveFieldValue(fieldValues, EMAIL);

        giveChecker(checker);

        List<VerificationMode> modes = setCheckModes(field);
        List<String> values = Arrays.asList(username, nickname, email);

        // when
        boolean result = registrationService.check(username, nickname, email);

        // then
        assertTrue(result);

        verifyExists(modes, values);
    }

    private void testCheckUserEnabled(String referer, String redirectUrl) {
        // given
        User user = giveUser();

        giveEnabled(user, true);

        // when
        String result = registrationService.checkUserEnabled(ID, referer);

        // then
        assertEquals(redirectUrl, result);

        verify(userService).findById(eq(ID));
        verify(user).isEnabled();
    }

    private RegistrationDto createRegistrationDto() {
        return RegistrationDto.builder()
                .username(USERNAME_VALUE).password(PASSWORD_VALUE).confirmPassword(PASSWORD_VALUE)
                .nickname(NICKNAME_VALUE).email(EMAIL_VALUE).build();
    }

    private void giveChecker(Function<String, Boolean> checker) {
        ServiceTestUtil.giveChecker(checker, false);
    }

    private User giveSendToken(boolean isRegistration) {
        User user = giveUser();

        when(tokensService.save(any(Tokens.class))).thenReturn(ID);

        if (isRegistration) {
            giveUsername(user);
        }

        when(emailService.setConfirmEmailText(anyString(), anyString())).thenReturn(TEXT);

        doNothing().when(emailService).send(anyString(), anyString());

        return user;
    }

    private User giveUser() {
        return ServiceTestUtil.giveUser(userService::findById, Long.class);
    }

    private void giveUsername(User user) {
        ServiceTestUtil.giveUsername(user, USERNAME_VALUE);
    }

    private List<Tokens> giveTokens() {
        List<Tokens> tokens = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            Tokens token = mock(Tokens.class);
            tokens.add(token);
        }

        return tokens;
    }

    private Map<Fields, String> createFieldValues(Fields field, String value) {
        Map<Fields, String> fieldValues = new HashMap<>();
        fieldValues.put(field, value);

        return fieldValues;
    }

    private String giveFieldValue(Map<Fields, String> fieldValues, Fields field) {
        return fieldValues.getOrDefault(field, null);
    }

    private void giveEnabled(User user, boolean isEnabled) {
        when(user.isEnabled()).thenReturn(isEnabled);
    }

    private void verifySendToken(VerificationMode findMode, User user, boolean isRegistration) {
        VerificationMode usernameMode = times(1);
        String username = USERNAME_VALUE;
        if (!isRegistration) {
            usernameMode = never();
            username = "";
        }

        verify(userService, findMode).findById(ID);
        verify(tokensService).save(any(Tokens.class));
        verify(user, usernameMode).getUsername();
        verify(emailService).setConfirmEmailText(eq(username), anyString());
        verify(emailService).send(eq(EMAIL_VALUE), eq(TEXT));
    }

    private void verifyExists(List<VerificationMode> modes, List<String> values) {
        verify(userService, modes.get(0)).existsByUsername(eq(values.get(0)));
        verify(userService, modes.get(1)).existsByNickname(eq(values.get(1)));
        verify(userService, modes.get(2)).existsByEmail(eq(values.get(2)));
    }

    private List<VerificationMode> setCheckModes(Fields field) {
        List<VerificationMode> modes = new ArrayList<>(Collections.nCopies(3, never()));

        switch (field) {
            case USERNAME -> modes.set(0, times(1));
            case NICKNAME -> modes.set(1, times(1));
            case EMAIL -> modes.set(2, times(1));
        }

        return modes;
    }
}
