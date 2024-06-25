package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.enums.SocialType;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.exception.CustomValidException;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.web.dto.user.EmailUpdateDto;
import com.eskgus.nammunity.web.dto.user.NicknameUpdateDto;
import com.eskgus.nammunity.web.dto.user.PasswordUpdateDto;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static com.eskgus.nammunity.domain.enums.Fields.*;
import static com.eskgus.nammunity.domain.enums.SocialType.NONE;
import static com.eskgus.nammunity.domain.enums.SocialType.convertSocialType;

@RequiredArgsConstructor
@Service
public class UserUpdateService {
    private final BCryptPasswordEncoder encoder;
    private final UserService userService;
    private final RegistrationService registrationService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final PrincipalHelper principalHelper;

    @Transactional
    public Long updatePassword(PasswordUpdateDto requestDto, Principal principal) {
        User user = principalHelper.getUserFromPrincipal(principal, true);

        validatePasswordUpdateDto(requestDto, user);

        encryptAndUpdatePassword(user, requestDto.getPassword());

        return user.getId();
    }

    @Transactional
    public Long updateEmail(EmailUpdateDto requestDto, Principal principal) {
        User user = principalHelper.getUserFromPrincipal(principal, true);

        String email = requestDto.getEmail();
        validateEmailUpdateDto(email, user);

        if (user.isEnabled()) { // 회원 정보 수정에서 최초 이메일 변경 (재발송 x)
            user.updateEnabled();
        }

        updateAndSendToken(user, email);

        user.updateEmail(email);

        return user.getId();
    }

    @Transactional
    public Long updateNickname(NicknameUpdateDto requestDto, Principal principal) {
        User user = principalHelper.getUserFromPrincipal(principal, true);

        validateNicknameUpdateDto(requestDto, user);

        user.updateNickname(requestDto.getNickname());

        return user.getId();
    }

    @Transactional
    public HttpHeaders deleteUser(Principal principal, String accessToken) {
        User user = principalHelper.getUserFromPrincipal(principal, true);
        Cookie cookie = resetCookie(user, accessToken);
        userService.delete(user.getId());

        return createHeaders(cookie);
    }

    @Transactional
    public HttpHeaders unlinkSocial(Principal principal, String social, String accessToken) {
        User user = principalHelper.getUserFromPrincipal(principal, true);

        SocialType socialType = convertSocialType(social);
        Cookie cookie = customOAuth2UserService.unlinkSocial(socialType, accessToken, user);

        return createHeaders(cookie);
    }

    @Transactional
    public void encryptAndUpdatePassword(User user, String password) {
        String encryptedPassword = registrationService.encryptPassword(password);
        user.updatePassword(encryptedPassword);
    }

    private void validatePasswordUpdateDto(PasswordUpdateDto passwordUpdateDto, User user) {
        String oldPassword = passwordUpdateDto.getOldPassword();
        String currentPassword = user.getPassword();
        String newPassword = passwordUpdateDto.getPassword();
        String confirmPassword = passwordUpdateDto.getConfirmPassword();

        if (!encoder.matches(oldPassword, currentPassword)) {
            throw new CustomValidException(OLD_PASSWORD, oldPassword, OLD_PASSWORD_MISMATCH);
        } else if (oldPassword.equals(newPassword)) {
            throw new CustomValidException(PASSWORD, newPassword, INVALID_NEW_PASSWORD);
        } else if (!newPassword.equals(confirmPassword)) {
            throw new CustomValidException(CONFIRM_PASSWORD, confirmPassword, CONFIRM_PASSWORD_MISMATCH);
        }
    }

    private void validateEmailUpdateDto(String email, User user) {
        if (user.getEmail().equals(email)) {
            throw new CustomValidException(EMAIL, email, INVALID_NEW_EMAIL);
        } else if (userService.existsByEmail(email)) {
            throw new CustomValidException(EMAIL, email, EMAIL_EXISTS);
        }
    }

    @Transactional
    private void updateAndSendToken(User user, String email) {
        user.getTokens().forEach(tokens -> tokens.updateExpiredAt(LocalDateTime.now()));
        registrationService.sendToken(user.getId(), email, "update");
    }

    private void validateNicknameUpdateDto(NicknameUpdateDto nicknameUpdateDto, User user) {
        String newNickname = nicknameUpdateDto.getNickname();

        if (user.getNickname().equals(newNickname)) {
            throw new CustomValidException(NICKNAME, newNickname, INVALID_NEW_NICKNAME);
        } else if (userService.existsByNickname(nicknameUpdateDto.getNickname())) {
            throw new CustomValidException(NICKNAME, newNickname, NICKNAME_EXISTS);
        }
    }

    @Transactional
    private Cookie resetCookie(User user, String accessToken) {
        if (NONE.equals(user.getSocial())) {
            return null;
        }
        return customOAuth2UserService.unlinkSocial(user.getSocial(), accessToken, user);
    }

    private HttpHeaders createHeaders(Cookie cookie) {
        return cookie != null ? createHeadersWithCookie(cookie) : null;
    }

    private HttpHeaders createHeadersWithCookie(Cookie cookie) {
        ResponseCookie responseCookie = ResponseCookie.from(cookie.getName(), cookie.getValue())
                .path(cookie.getPath())
                .httpOnly(cookie.isHttpOnly())
                .maxAge(cookie.getMaxAge()).build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, responseCookie.toString());

        return headers;
    }
}
