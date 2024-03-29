package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.user.EmailUpdateDto;
import com.eskgus.nammunity.web.dto.user.NicknameUpdateDto;
import com.eskgus.nammunity.web.dto.user.PasswordUpdateDto;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class UserUpdateService {
    private final BCryptPasswordEncoder encoder;
    private final UserRepository userRepository;
    private final RegistrationService registrationService;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Transactional
    public void updatePassword(PasswordUpdateDto requestDto, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new
                IllegalArgumentException("username"));

        String oldPassword = requestDto.getOldPassword();
        String currentPassword = user.getPassword();
        String newPassword = requestDto.getPassword();

        if (!encoder.matches(oldPassword, currentPassword)) {
            throw new IllegalArgumentException("oldPassword");
        } else if (oldPassword.equals(newPassword)) {
            throw new IllegalArgumentException("password");
        } else if (!newPassword.equals(requestDto.getConfirmPassword())) {
            throw new IllegalArgumentException("confirmPassword");
        }

        user.updatePassword(encoder.encode(newPassword));
    }

    @Transactional
    public void updateEmail(EmailUpdateDto requestDto, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new
                IllegalArgumentException("존재하지 않는 ID입니다."));
        String email = requestDto.getEmail();

        if (user.isEnabled()) {
            if (user.getEmail().equals(email)) {
                throw new IllegalArgumentException("현재 이메일과 같습니다.");
            } else if (userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }
            user.updateEnabled();
        }

        user.getTokens().forEach(tokens -> tokens.updateExpiredAt(LocalDateTime.now()));
        registrationService.sendToken(user.getId(), email, "update");
        user.updateEmail(email);
    }

    @Transactional
    public void updateNickname(NicknameUpdateDto requestDto, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new
                IllegalArgumentException("존재하지 않는 ID입니다."));

        if (user.getNickname().equals(requestDto.getNickname())) {
            throw new IllegalArgumentException("현재 닉네임과 같습니다.");
        } else if (userRepository.existsByNickname(requestDto.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        user.updateNickname(requestDto.getNickname());
    }

    @Transactional
    public Cookie deleteUser(String username, String accessToken) {
        Cookie cookie = null;

        User user = userRepository.findByUsername(username).orElseThrow(() -> new
                IllegalArgumentException("존재하지 않는 ID입니다."));
        if (!user.getSocial().equals("none")) {
            cookie = customOAuth2UserService.unlinkSocial(username, user.getSocial(), accessToken);
        }
        userRepository.delete(user);
        return cookie;
    }
}
