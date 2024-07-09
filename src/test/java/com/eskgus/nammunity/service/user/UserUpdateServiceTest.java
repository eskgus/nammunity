package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.enums.Fields;
import com.eskgus.nammunity.domain.enums.SocialType;
import com.eskgus.nammunity.domain.tokens.Tokens;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.util.ServiceTestUtil;
import com.eskgus.nammunity.web.dto.user.EmailUpdateDto;
import com.eskgus.nammunity.web.dto.user.NicknameUpdateDto;
import com.eskgus.nammunity.web.dto.user.PasswordUpdateDto;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.eskgus.nammunity.domain.enums.Fields.*;
import static com.eskgus.nammunity.domain.enums.SocialType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserUpdateServiceTest {
    @Mock
    private BCryptPasswordEncoder encoder;

    @Mock
    private UserService userService;

    @Mock
    private RegistrationService registrationService;

    @Mock
    private CustomOAuth2UserService customOAuth2UserService;

    @Mock
    private PrincipalHelper principalHelper;

    @InjectMocks
    private UserUpdateService userUpdateService;

    private static final Long ID = 1L;
    private static final String PASSWORD_VALUE = PASSWORD.getKey() + ID;
    private static final String ACCESS_TOKEN = Fields.ACCESS_TOKEN.getKey();

    @Test
    public void updatePassword() {
        // given
        PasswordUpdateDto requestDto = createPasswordUpdateDto();

        Pair<Principal, User> pair = givePrincipal();
        Principal principal = pair.getFirst();
        User user = pair.getSecond();

        String currentPassword = requestDto.getOldPassword();
        when(user.getPassword()).thenReturn(currentPassword);

        when(encoder.matches(anyString(), anyString())).thenReturn(true);

        String encryptedPassword = giveEncryptPassword(requestDto.getPassword());

        doNothing().when(user).updatePassword(anyString());

        giveUserId(user);

        // when
        Long result = userUpdateService.updatePassword(requestDto, principal);

        // then
        assertEquals(user.getId(), result);

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(encoder).matches(eq(requestDto.getOldPassword()), eq(currentPassword));
        verify(registrationService).encryptPassword(eq(requestDto.getPassword()));
        verify(user).updatePassword(eq(encryptedPassword));
    }

    @Test
    public void updateEmailWithEnabledUser() {
        testUpdateEmail(true);
    }

    @Test
    public void updateEmailWithDisabledUser() {
        testUpdateEmail(false);
    }

    private void testUpdateEmail(boolean isEnabled) {
        // given
        String emailPrefix = EMAIL.getKey() + ID;
        String emailSuffix = "@naver.com";
        String newEmail = emailPrefix + ID + emailSuffix;
        EmailUpdateDto requestDto = new EmailUpdateDto(newEmail);

        Pair<Principal, User> pair = givePrincipal();
        Principal principal = pair.getFirst();
        User user = pair.getSecond();

        when(user.getEmail()).thenReturn(emailPrefix + emailSuffix);

        giveChecker(userService::existsByEmail);

        when(user.isEnabled()).thenReturn(isEnabled);

        if (isEnabled) {
            doNothing().when(user).updateEnabled();
        }

        List<Tokens> tokens = giveTokens();
        when(user.getTokens()).thenReturn(tokens);

        for (Tokens token : tokens) {
            doNothing().when(token).updateExpiredAt(any(LocalDateTime.class));
        }

        doNothing().when(registrationService).sendToken(anyLong(), anyString(), anyString());

        doNothing().when(user).updateEmail(anyString());

        giveUserId(user);

        VerificationMode updateEnabledMode = isEnabled ? times(1) : never();

        // when
        Long result = userUpdateService.updateEmail(requestDto, principal);

        // then
        assertEquals(user.getId(), result);

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(user).getEmail();
        verify(userService).existsByEmail(eq(newEmail));
        verify(user).isEnabled();
        verify(user, updateEnabledMode).updateEnabled();
        verify(user).getTokens();
        tokens.forEach(token -> verify(token).updateExpiredAt(any(LocalDateTime.class)));
        verify(registrationService).sendToken(eq(user.getId()), eq(newEmail), eq("update"));
        verify(user).updateEmail(newEmail);
    }

    @Test
    public void updateNickname() {
        // given
        String nicknameValue = NICKNAME.getKey() + ID;
        String newNickname = nicknameValue + ID;
        NicknameUpdateDto requestDto = new NicknameUpdateDto(newNickname);

        Pair<Principal, User> pair = givePrincipal();
        Principal principal = pair.getFirst();
        User user = pair.getSecond();

        when(user.getNickname()).thenReturn(nicknameValue);

        giveChecker(userService::existsByNickname);

        doNothing().when(user).updateNickname(anyString());

        giveUserId(user);

        // when
        Long result = userUpdateService.updateNickname(requestDto, principal);

        // then
        assertEquals(user.getId(), result);

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(user).getNickname();
        verify(userService).existsByNickname(eq(newNickname));
        verify(user).updateNickname(eq(newNickname));
    }

    @Test
    public void deleteRegularUser() {
        HttpHeaders result = testDeleteUser(NONE, times(1), never());

        assertNull(result);
    }

    @Test
    public void deleteSocialUser() {
        giveUnlinkSocial();

        HttpHeaders result = testDeleteUser(GOOGLE, times(2), times(1));

        assertNotNull(result);
    }

    @Test
    public void unlinkSocial() {
        // given
        Pair<Principal, User> pair = givePrincipal();
        Principal principal = pair.getFirst();
        User user = pair.getSecond();

        SocialType socialType = GOOGLE;

        giveUnlinkSocial();

        // when
        HttpHeaders result = userUpdateService.unlinkSocial(principal, socialType.getKey(), ACCESS_TOKEN);

        // then
        assertNotNull(result);

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(customOAuth2UserService).unlinkSocial(eq(socialType), eq(ACCESS_TOKEN), eq(user));
    }

    @Test
    public void encryptAndUpdatePassword() {
        // given
        User user = mock(User.class);

        String encryptedPassword = giveEncryptPassword(PASSWORD_VALUE);

        doNothing().when(user).updatePassword(anyString());

        // when
        userUpdateService.encryptAndUpdatePassword(user, PASSWORD_VALUE);

        // then
        verify(registrationService).encryptPassword(eq(PASSWORD_VALUE));
        verify(user).updatePassword(eq(encryptedPassword));
    }

    private HttpHeaders testDeleteUser(SocialType socialType,
                                       VerificationMode getSocialMode, VerificationMode unlinkSocialMode) {
        // given
        Pair<Principal, User> pair = givePrincipal();
        Principal principal = pair.getFirst();
        User user = pair.getSecond();

        giveSocial(user, socialType);

        doNothing().when(userService).delete(anyLong());

        // when
        HttpHeaders result = userUpdateService.deleteUser(principal, ACCESS_TOKEN);

        // then
        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(user, getSocialMode).getSocial();
        verify(customOAuth2UserService, unlinkSocialMode).unlinkSocial(eq(socialType), eq(ACCESS_TOKEN), eq(user));
        verify(userService).delete(eq(user.getId()));

        return result;
    }

    private PasswordUpdateDto createPasswordUpdateDto() {
        String oldPassword = PASSWORD_VALUE;
        String password = oldPassword + ID;

        return PasswordUpdateDto.builder()
                .oldPassword(oldPassword).password(password).confirmPassword(password).build();
    }

    private Pair<Principal, User> givePrincipal() {
        return ServiceTestUtil.givePrincipal(principalHelper::getUserFromPrincipal);
    }

    private List<Tokens> giveTokens() {
        List<Tokens> tokens = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            Tokens token = mock(Tokens.class);
            tokens.add(token);
        }

        return tokens;
    }

    private void giveUserId(User user) {
        when(user.getId()).thenReturn(ID);
    }

    private void giveChecker(Function<String, Boolean> checker) {
        ServiceTestUtil.giveChecker(checker, false);
    }

    private void giveSocial(User user, SocialType socialType) {
        when(user.getSocial()).thenReturn(socialType);
    }

    private void giveUnlinkSocial() {
        Cookie cookie = createCookie();
        when(customOAuth2UserService.unlinkSocial(any(SocialType.class), anyString(), any(User.class)))
                .thenReturn(cookie);
    }

    private String giveEncryptPassword(String password) {
        String encryptedPassword = "encrypted" + password;
        when(registrationService.encryptPassword(anyString())).thenReturn(encryptedPassword);

        return encryptedPassword;
    }

    private Cookie createCookie() {
        return new Cookie("access_token", ACCESS_TOKEN);
    }
}
