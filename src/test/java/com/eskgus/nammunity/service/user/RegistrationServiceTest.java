package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.tokens.Tokens;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.exception.CustomValidException;
import com.eskgus.nammunity.service.email.EmailService;
import com.eskgus.nammunity.service.tokens.TokensService;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

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

    @Test
    public void checkUserEnabledInSignIn() {
        testCheckUserEnabled("/users/sign-up", "/users/sign-in");
    }

    @Test
    public void checkUserEnabledInMyPage() {
        String myPage = "/users/my-page/update/user-info";
        testCheckUserEnabled(myPage, myPage);
    }

    @Test
    public void checkUserEnabledWithNonExistentUserId() {
        // given
        when(userService.findById(anyLong())).thenThrow(IllegalArgumentException.class);

        Long userId = 1L;

        // when/then
        assertThrows(IllegalArgumentException.class, () -> registrationService.checkUserEnabled(userId, "/users/sign-up"));

        verify(userService).findById(eq(userId));
    }

    @Test
    public void checkUserEnabledWithNotEnabledUser() {
        // given
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.isEnabled()).thenReturn(false);
        when(userService.findById(anyLong())).thenReturn(user);

        // when/then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> registrationService.checkUserEnabled(user.getId(), "/users/sign-up"));
        assertEquals("인증되지 않은 이메일입니다.", exception.getMessage());

        verify(userService).findById(eq(user.getId()));
        verify(user).isEnabled();
    }

    @Test
    public void resendToken() {
        // given
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.isEnabled()).thenReturn(false);
        when(user.getCreatedDate()).thenReturn(LocalDateTime.now());
        when(user.getEmail()).thenReturn("email@naver.com");
        when(user.getUsername()).thenReturn("username");
        when(userService.findById(user.getId())).thenReturn(user);

        Tokens token = mock(Tokens.class);
        List<Tokens> tokens = Collections.singletonList(token);
        when(user.getTokens()).thenReturn(tokens);

        when(emailService.setConfirmEmailText(eq(user.getUsername()), anyString())).thenReturn("text");

        // when
        registrationService.resendToken(user.getId());

        // then
        verify(userService, times(2)).findById(eq(user.getId()));
        verify(token).updateExpiredAt(any(LocalDateTime.class));
        verify(tokensService).save(any(Tokens.class));
        verify(emailService).setConfirmEmailText(eq(user.getUsername()), anyString());
        verify(emailService).send(eq(user.getEmail()), anyString());
    }

    @Test
    public void resendTokenWithNonExistentUserId() {
        // given
        when(userService.findById(anyLong())).thenThrow(IllegalArgumentException.class);

        User user = createMockedUser();

        // when/then
        assertThrowsAndVerifyResendToken(user, never(), never());
    }

    @Test
    public void resendTokenWithEnabledUser() {
        // given
        User user = createMockedUser();
        when(user.isEnabled()).thenReturn(true);
        when(userService.findById(eq(user.getId()))).thenReturn(user);

        // when/then
        assertThrowsAndVerifyResendToken(user, times(1), never());
    }

    @Test
    public void resendTokenWithInvalidUserCreatedDate() {
        // given
        User user = createMockedUser();
        when(user.isEnabled()).thenReturn(false);
        when(user.getCreatedDate()).thenReturn(LocalDateTime.now().minusMinutes(13));
        when(userService.findById(eq(user.getId()))).thenReturn(user);

        // when/then
        assertThrowsAndVerifyResendToken(user, times(1), times(1));
    }

    @Test
    public void signUp() {
        // given
        RegistrationDto registrationDto = giveEmail(false);

        User user = giveAfterEmail();
        when(user.getUsername()).thenReturn(registrationDto.getUsername());
        when(userService.findById(user.getId())).thenReturn(user);

        when(emailService.setConfirmEmailText(eq(user.getUsername()), anyString())).thenReturn("text");

        // when
        Long result = registrationService.signUp(registrationDto);

        // then
        assertEquals(user.getId(), result);

        verifyExistsField(null, registrationDto);
        verifySaveUser(times(1), registrationDto.getPassword(), user.getId());
        verifySaveToken(times(1), user.getUsername(), registrationDto.getEmail());
    }

    @Test
    public void signUpWithExistentUsername() {
        // given
        RegistrationDto registrationDto = giveCommon(true, true);

        // when/then
        assertThrowsAndVerifySignUp("username", registrationDto);
    }

    @Test
    public void signUpWithInvalidConfirmPassword() {
        // given
        RegistrationDto registrationDto = giveCommon(false, false);

        // when/then
        assertThrowsAndVerifySignUp("username", registrationDto);
    }

    @Test
    public void signUpWithExistentNickname() {
        // given
        RegistrationDto registrationDto = giveCommon(true, false);
        when(userService.existsByNickname(anyString())).thenReturn(true);

        // when/then
        assertThrowsAndVerifySignUp("nickname", registrationDto);
    }

    @Test
    public void signUpWithExistentEmail() {
        // given
        RegistrationDto registrationDto = giveEmail(true);

        // when/then
        assertThrowsAndVerifySignUp(null, registrationDto);
    }

    @Test
    public void signUpWithNonExistentUserId() {
        // given
        RegistrationDto registrationDto = giveEmail(false);

        User user = giveAfterEmail();
        when(userService.findById(anyLong())).thenThrow(IllegalArgumentException.class);

        // when/then
        assertThrows(IllegalArgumentException.class, () -> registrationService.signUp(registrationDto));

        verifyExistsFieldAndNoSaveToken(null, registrationDto);
        verifySaveUser(times(1), registrationDto.getPassword(), user.getId());
    }

    @Test
    public void checkUsername() {
        // given
        when(userService.existsByUsername(anyString())).thenReturn(false);

        String username = "username";

        // when
        boolean result = registrationService.check(username, null, null);

        // then
        assertTrue(result);

        verify(userService).existsByUsername(eq(username));
    }

    @Test
    public void checkNickname() {
        // given
        when(userService.existsByNickname(anyString())).thenReturn(false);

        String nickname = "nickname";

        // when
        boolean result = registrationService.check(null, nickname, null);

        // then
        assertTrue(result);

        verify(userService).existsByNickname(eq(nickname));
    }

    @Test
    public void checkEmail() {
        // given
        when(userService.existsByEmail(anyString())).thenReturn(false);

        String email = "email@naver.com";

        // when
        boolean result = registrationService.check(null, null, email);

        // then
        assertTrue(result);

        verify(userService).existsByEmail(eq(email));
    }

    @Test
    public void checkWithBlankUsername() {
        // given
        // when/then
        assertThrowsAndVerifyCheck("", null, null);
    }

    @Test
    public void checkWithExistentUsername() {
        // given
        when(userService.existsByUsername(anyString())).thenReturn(true);

        // when/then
        assertThrowsAndVerifyCheck("username", null, null);

    }

    @Test
    public void checkWithBlankNickname() {
        // given
        // when/then
        assertThrowsAndVerifyCheck(null, "", null);
    }

    @Test
    public void checkWithExistentNickname() {
        // given
        when(userService.existsByNickname(anyString())).thenReturn(true);

        // when/then
        assertThrowsAndVerifyCheck(null, "nickname", null);
    }

    @Test
    public void checkWithBlankEmail() {
        // given
        // when/then
        assertThrowsAndVerifyCheck(null, null, "");
    }

    @Test
    public void checkWithExistentEmail() {
        // given
        when(userService.existsByEmail(anyString())).thenReturn(true);

        // when/then
        assertThrowsAndVerifyCheck(null, null, "email");
    }

    private void testCheckUserEnabled(String referer, String redirectPage) {
        // given
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.isEnabled()).thenReturn(true);
        when(userService.findById(user.getId())).thenReturn(user);

        // when
        String result = registrationService.checkUserEnabled(user.getId(), referer);

        // then
        assertEquals(redirectPage, result);

        verify(userService).findById(eq(user.getId()));
        verify(user).isEnabled();
    }

    private User createMockedUser() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        return user;
    }

    private void assertThrowsAndVerifyResendToken(User user,
                                                  VerificationMode enabledMode, VerificationMode createdDateMode) {
        assertThrows(IllegalArgumentException.class, () -> registrationService.resendToken(user.getId()));

        verify(userService).findById(eq(user.getId()));
        verify(user, enabledMode).isEnabled();
        verify(user, createdDateMode).getCreatedDate();
    }

    private RegistrationDto giveEmail(boolean doesEmailExist) {
        RegistrationDto registrationDto = giveCommon(true, false);
        giveAfterUsername(doesEmailExist);

        return registrationDto;
    }

    private RegistrationDto giveCommon(boolean isPasswordConfirmed, boolean doesUsernameExist) {
        RegistrationDto registrationDto = createRegistrationDto(isPasswordConfirmed);
        when(userService.existsByUsername(anyString())).thenReturn(doesUsernameExist);

        return registrationDto;
    }

    private void giveAfterUsername(boolean doesEmailExist) {
        when(userService.existsByNickname(anyString())).thenReturn(false);
        when(userService.existsByEmail(anyString())).thenReturn(doesEmailExist);
    }

    private RegistrationDto createRegistrationDto(boolean isPasswordConfirmed) {
        String username = "username";
        String password = "password";
        String confirmPassword = isPasswordConfirmed ? password : "confirm" + password;
        return RegistrationDto.builder()
                .username(username).password("password").confirmPassword(confirmPassword)
                .nickname("nickname").email("email@naver.com").build();
    }

    private User giveAfterEmail() {
        when(encoder.encode(anyString())).thenReturn("encryptedPassword");

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userService.save(any(RegistrationDto.class))).thenReturn(1L);

        return user;
    }

    private void assertThrowsAndVerifySignUp(String field, RegistrationDto registrationDto) {
        assertThrows(CustomValidException.class, () -> registrationService.signUp(registrationDto));

        verifyExistsFieldAndNoSaveToken(field, registrationDto);
        verifySaveUser(never(), "dummyPassword", 1L);
    }

    private void verifyExistsFieldAndNoSaveToken(String field, RegistrationDto registrationDto) {
        verifyExistsField(field, registrationDto);
        verifySaveToken(never(), "dummyUsername", "dummyEmail");
    }

    private void verifySaveUser(VerificationMode mode, String password, Long userId) {
        verify(encoder, mode).encode(eq(password));
        verify(userService, mode).save(any(RegistrationDto.class));
        verify(userService, mode).findById(eq(userId));
    }

    private void verifyExistsField(String field, RegistrationDto registrationDto) {
        boolean isFieldUsername = "username".equals(field);
        boolean isFieldNotNull = field != null;

        VerificationMode nicknameMode = isFieldUsername ? never() : times(1);
        String nickname = isFieldUsername ? "dummyNickname" : registrationDto.getNickname();

        VerificationMode emailMode = isFieldNotNull ? never() : times(1);
        String email = isFieldNotNull ? "dummyEmail" : registrationDto.getEmail();

        verify(userService, times(1)).existsByUsername(eq(registrationDto.getUsername()));
        verify(userService, nicknameMode).existsByNickname(eq(nickname));
        verify(userService, emailMode).existsByEmail(eq(email));
    }

    private void verifySaveToken(VerificationMode mode, String username, String email) {
        verify(tokensService, mode).save(any(Tokens.class));
        verify(emailService, mode).setConfirmEmailText(eq(username), anyString());
        verify(emailService, mode).send(eq(email), anyString());
    }

    private void assertThrowsAndVerifyCheck(String username, String nickname, String email) {
        assertThrows(CustomValidException.class, () -> registrationService.check(username, nickname, email));

        if (isNotBlank(username)) {
            verify(userService).existsByUsername(eq(username));
        } else if (isNotBlank(nickname)) {
            verify(userService).existsByNickname(eq(nickname));
        } else if (isNotBlank(email)) {
            verify(userService).existsByEmail(eq(email));
        } else {
            verify(userService, never()).existsByUsername(anyString());
            verify(userService, never()).existsByNickname(anyString());
            verify(userService, never()).existsByEmail(anyString());
        }
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
