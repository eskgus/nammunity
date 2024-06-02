package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.tokens.Tokens;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.exception.CustomValidException;
import com.eskgus.nammunity.service.email.EmailService;
import com.eskgus.nammunity.service.tokens.TokensService;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RegistrationService {
    private final UserService userService;
    private final BCryptPasswordEncoder encoder;
    private final EmailService emailService;
    private final TokensService tokensService;

    @Transactional
    public Long signUp(RegistrationDto registrationDto) {
        validateRegistrationDto(registrationDto);

        RegistrationDto encryptedRegistrationDto = encryptRegistrationDto(registrationDto);
        Long id = userService.save(encryptedRegistrationDto);

        sendToken(id, encryptedRegistrationDto.getEmail(), "registration");

        return id;
    }

    @Transactional(readOnly = true)
    private void validateRegistrationDto(RegistrationDto registrationDto) {
        if (userService.existsByUsername(registrationDto.getUsername())) {
            throw new CustomValidException("username", registrationDto.getUsername(), "이미 사용 중인 ID입니다.");
        }
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new CustomValidException("confirmPassword", registrationDto.getConfirmPassword(), "비밀번호가 일치하지 않습니다.");
        }
        if (userService.existsByNickname(registrationDto.getNickname())) {
            throw new CustomValidException("nickname", registrationDto.getNickname(), "이미 사용 중인 닉네임입니다.");
        }
        if (userService.existsByEmail(registrationDto.getEmail())) {
            throw new CustomValidException("email", registrationDto.getEmail(), "이미 사용 중인 이메일입니다.");
        }
    }

    private RegistrationDto encryptRegistrationDto(RegistrationDto registrationDto) {
        String encryptedPassword = encryptPassword(registrationDto.getPassword());
        return RegistrationDto.builder()
                .username(registrationDto.getUsername()).password(encryptedPassword)
                .nickname(registrationDto.getNickname())
                .email(registrationDto.getEmail())
                .role(Role.USER).build();
    }

    public String encryptPassword(String password) {
        return encoder.encode(password);
    }

    @Transactional
    public void sendToken(Long id, String email, String purpose) {
        User user = userService.findById(id);

        String token = createAndSaveToken(user);

        sendConfirmEmail(purpose, user, token, email);
    }

    @Transactional
    private String createAndSaveToken(User user) {
        String uuid = UUID.randomUUID().toString();
        Tokens token = Tokens.builder()
                .token(uuid).user(user)
                .createdAt(LocalDateTime.now()).expiredAt(LocalDateTime.now().plusMinutes(3))
                .build();
        tokensService.save(token);

        return token.getToken();
    }

    private void sendConfirmEmail(String purpose, User user, String token, String email) {
        String username = purpose.equals("update") ? "" : user.getUsername();
        String text = emailService.setConfirmEmailText(username, token);
        emailService.send(email, text);
    }

    @Transactional
    public void confirmToken(String token) {
        Tokens confirmationToken = tokensService.findByToken(token);

        validateToken(confirmationToken);

        updateTokenAndUser(confirmationToken);
    }

    private void validateToken(Tokens token) {
        if (token.getConfirmedAt() != null) {
            throw new IllegalArgumentException("이미 인증된 이메일입니다.");
        }
        if (token.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("인증 링크가 만료됐습니다.");
        }
    }

    @Transactional
    private void updateTokenAndUser(Tokens token) {
        token.updateConfirmedAt(LocalDateTime.now());
        token.getUser().updateEnabled();
    }

    @Transactional(readOnly = true)
    public boolean check(String username, String nickname, String email) {
        if (username != null) {
            checkUsername(username);
        } else if (nickname != null) {
            checkNickname(nickname);
        } else if (email != null) {
            checkEmail(email);
        }
        return true;
    }

    private void checkUsername(String username) {
        if (username.isBlank()) {
            throw new CustomValidException("username", username, "ID를 입력하세요.");
        }
        if (userService.existsByUsername(username)) {
            throw new CustomValidException("username", username, "이미 사용 중인 ID입니다.");
        }
    }

    private void checkNickname(String nickname) {
        if (nickname.isBlank()) {
            throw new CustomValidException("nickname", nickname, "닉네임을 입력하세요.");
        }
        if (userService.existsByNickname(nickname)) {
            throw new CustomValidException("nickname", nickname, "이미 사용 중인 닉네임입니다.");
        }
    }

    private void checkEmail(String email) {
        if (email.isBlank()) {
            throw new CustomValidException("email", email, "이메일을 입력하세요.");
        }
        if (userService.existsByEmail(email)) {
            throw new CustomValidException("email", email, "이미 사용 중인 이메일입니다.");
        }
    }

    @Transactional
    public void resendToken(Long id) {
        User user = userService.findById(id);

        checkTokenResendAvailability(user);

        user.getTokens().forEach(tokens -> tokens.updateExpiredAt(LocalDateTime.now()));
        sendToken(id, user.getEmail(), "registration");
    }

    private void checkTokenResendAvailability(User user) {
        if (user.isEnabled()) {
            throw new IllegalArgumentException("이미 인증된 이메일입니다.");
        } else if (LocalDateTime.now().isAfter(user.getCreatedDate().plusMinutes(12))) {
            throw new IllegalArgumentException("더 이상 재발송할 수 없어요. 다시 가입해 주세요.");
        }
    }

    @Transactional(readOnly = true)
    public String checkUserEnabled(Long id, String referer) {
        User user = userService.findById(id);
        boolean enabled = user.isEnabled();

        if (!enabled) {
            throw new IllegalArgumentException("인증되지 않은 이메일입니다.");
        } else {
            if (referer.contains("/sign-up")) {
                return "/users/sign-in";
            } else {
                return "/users/my-page/update/user-info";
            }
        }
    }
}
