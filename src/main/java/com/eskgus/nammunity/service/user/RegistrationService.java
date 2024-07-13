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

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static com.eskgus.nammunity.domain.enums.Fields.*;

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

    @Transactional
    public void resendToken(Long id) {
        User user = findUsersById(id);

        checkTokenResendAvailability(user);

        user.getTokens().forEach(tokens -> tokens.updateExpiredAt(LocalDateTime.now()));
        sendToken(id, user.getEmail(), "registration");
    }

    @Transactional
    public void sendToken(Long id, String email, String purpose) {
        User user = findUsersById(id);

        String token = createAndSaveToken(user);

        sendConfirmEmail(purpose, user, token, email);
    }

    public String encryptPassword(String password) {
        return encoder.encode(password);
    }

    @Transactional
    public void confirmToken(String token) {
        Tokens confirmationToken = tokensService.findByToken(token);

        validateToken(confirmationToken);

        updateTokenAndUser(confirmationToken);
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

    @Transactional(readOnly = true)
    public String checkUserEnabled(Long id, String referer) {
        User user = findUsersById(id);
        boolean enabled = user.isEnabled();

        if (!enabled) {
            throw new IllegalArgumentException(EMAIL_NOT_CONFIRMED.getMessage());
        } else {
            if (referer.contains("/sign-up")) {
                return "/users/sign-in";
            } else {
                return "/users/my-page/update/user-info";
            }
        }
    }

    private User findUsersById(Long userId) {
        return userService.findById(userId);
    }

    private void validateRegistrationDto(RegistrationDto registrationDto) {
        if (existsUsersByUsername(registrationDto.getUsername())) {
            throw new CustomValidException(USERNAME, registrationDto.getUsername(), USERNAME_EXISTS);
        }
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new CustomValidException(CONFIRM_PASSWORD, registrationDto.getConfirmPassword(), CONFIRM_PASSWORD_MISMATCH);
        }
        if (existsUsersByNickname(registrationDto.getNickname())) {
            throw new CustomValidException(NICKNAME, registrationDto.getNickname(), NICKNAME_EXISTS);
        }
        if (existsUsersByEmail(registrationDto.getEmail())) {
            throw new CustomValidException(EMAIL, registrationDto.getEmail(), EMAIL_EXISTS);
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

    private void checkTokenResendAvailability(User user) {
        if (user.isEnabled()) {
            throw new IllegalArgumentException(EMAIL_CONFIRMED.getMessage());
        } else if (LocalDateTime.now().isAfter(user.getCreatedDate().plusMinutes(12))) {
            throw new IllegalArgumentException(RESEND_NOT_ALLOWED.getMessage());
        }
    }

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

    private void validateToken(Tokens token) {
        if (token.getConfirmedAt() != null) {
            throw new IllegalArgumentException(EMAIL_CONFIRMED.getMessage());
        }
        if (token.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(TOKEN_EXPIRED.getMessage());
        }
    }

    private void updateTokenAndUser(Tokens token) {
        token.updateConfirmedAt(LocalDateTime.now());
        token.getUser().updateEnabled();
    }

    private void checkUsername(String username) {
        if (username.isBlank()) {
            throw new CustomValidException(USERNAME, username, EMPTY_USERNAME);
        }
        if (existsUsersByUsername(username)) {
            throw new CustomValidException(USERNAME, username, USERNAME_EXISTS);
        }
    }

    private void checkNickname(String nickname) {
        if (nickname.isBlank()) {
            throw new CustomValidException(NICKNAME, nickname, EMPTY_NICKNAME);
        }
        if (existsUsersByNickname(nickname)) {
            throw new CustomValidException(NICKNAME, nickname, NICKNAME_EXISTS);
        }
    }

    private void checkEmail(String email) {
        if (email.isBlank()) {
            throw new CustomValidException(EMAIL, email, EMPTY_EMAIL);
        }
        if (existsUsersByEmail(email)) {
            throw new CustomValidException(EMAIL, email, EMAIL_EXISTS);
        }
    }

    private boolean existsUsersByUsername(String username) {
        return userService.existsByUsername(username);
    }

    private boolean existsUsersByNickname(String nickname) {
        return userService.existsByNickname(nickname);
    }

    private boolean existsUsersByEmail(String email) {
        return userService.existsByEmail(email);
    }
}
