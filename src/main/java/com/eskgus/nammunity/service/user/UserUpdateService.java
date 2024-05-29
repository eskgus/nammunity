package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.exception.CustomValidException;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.web.dto.user.EmailUpdateDto;
import com.eskgus.nammunity.web.dto.user.NicknameUpdateDto;
import com.eskgus.nammunity.web.dto.user.PasswordUpdateDto;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class UserUpdateService {
    private final BCryptPasswordEncoder encoder;
    private final UserService userService;
    private final RegistrationService registrationService;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private PrincipalHelper principalHelper;

    @Transactional
    public Long updatePassword(PasswordUpdateDto requestDto, Principal principal) {
        User user = principalHelper.getUserFromPrincipal(principal, true);

        validatePasswordUpdateDto(requestDto, user);

        encryptAndUpdatePassword(user, requestDto.getPassword());

        return user.getId();
    }

    private void validatePasswordUpdateDto(PasswordUpdateDto passwordUpdateDto, User user) {
        String oldPassword = passwordUpdateDto.getOldPassword();
        String currentPassword = user.getPassword();
        String newPassword = passwordUpdateDto.getPassword();
        String confirmPassword = passwordUpdateDto.getConfirmPassword();

        if (!encoder.matches(oldPassword, currentPassword)) {
            throw new CustomValidException("oldPassword", oldPassword, "현재 비밀번호가 일치하지 않습니다.");
        } else if (oldPassword.equals(newPassword)) {
            throw new CustomValidException("password", newPassword, "현재 비밀번호와 새 비밀번호가 같으면 안 됩니다.");
        } else if (!newPassword.equals(confirmPassword)) {
            throw new CustomValidException("confirmPassword", confirmPassword, "비밀번호가 일치하지 않습니다.");
        }
    }

    @Transactional
    public void encryptAndUpdatePassword(User user, String password) {
        String encryptedPassword = encryptPassword(password);
        user.updatePassword(encryptedPassword);
    }

    private String encryptPassword(String password) {
        return encoder.encode(password);
    }

    @Transactional
    public Long updateEmail(EmailUpdateDto requestDto, Principal principal) {
        User user = principalHelper.getUserFromPrincipal(principal, true);

        String email = requestDto.getEmail();

        if (user.isEnabled()) {
            validateEmailUpdateDto(email, user);
            user.updateEnabled();
        }

        updateAndSendToken(user, email);

        user.updateEmail(email);

        return user.getId();
    }

    private void validateEmailUpdateDto(String email, User user) {
        if (user.getEmail().equals(email)) {
            throw new CustomValidException("email", email, "현재 이메일과 같습니다.");
        } else if (userService.existsByEmail(email)) {
            throw new CustomValidException("email", email, "이미 사용 중인 이메일입니다.");
        }
    }

    @Transactional
    private void updateAndSendToken(User user, String email) {
        user.getTokens().forEach(tokens -> tokens.updateExpiredAt(LocalDateTime.now()));
        registrationService.sendToken(user.getId(), email, "update");
    }

    @Transactional
    public Long updateNickname(NicknameUpdateDto requestDto, Principal principal) {
        User user = principalHelper.getUserFromPrincipal(principal, true);

        validateNicknameUpdateDto(requestDto, user);

        user.updateNickname(requestDto.getNickname());

        return user.getId();
    }

    private void validateNicknameUpdateDto(NicknameUpdateDto nicknameUpdateDto, User user) {
        String newNickname = nicknameUpdateDto.getNickname();

        if (user.getNickname().equals(newNickname)) {
            throw new CustomValidException("nickname", newNickname, "현재 닉네임과 같습니다.");
        } else if (userService.existsByNickname(nicknameUpdateDto.getNickname())) {
            throw new CustomValidException("nickname", newNickname, "이미 사용 중인 닉네임입니다.");
        }
    }

    @Transactional
    public HttpHeaders deleteUser(Principal principal, String accessToken) {
        User user = principalHelper.getUserFromPrincipal(principal, true);
        Cookie cookie = resetCookie(user, accessToken);
        userService.delete(user.getId());

        return createHeaders(cookie);
    }

    @Transactional
    private Cookie resetCookie(User user, String accessToken) {
        if (user.getSocial().equals("none")) {
            return null;
        }
        return customOAuth2UserService.unlinkSocial(user.getUsername(), user.getSocial(), accessToken);
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

    @Transactional
    public HttpHeaders unlinkSocial(Principal principal, String social, String accessToken) {
        User user = principalHelper.getUserFromPrincipal(principal, true);

        Cookie cookie = customOAuth2UserService.unlinkSocial(user.getUsername(), social, accessToken);

        return createHeaders(cookie);
    }
}
