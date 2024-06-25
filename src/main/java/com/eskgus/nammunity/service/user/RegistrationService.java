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
        User user = userService.findById(id);

        checkTokenResendAvailability(user);

        user.getTokens().forEach(tokens -> tokens.updateExpiredAt(LocalDateTime.now()));
        sendToken(id, user.getEmail(), "registration");
    }

    @Transactional
    public void sendToken(Long id, String email, String purpose) {
        User user = userService.findById(id);

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
        User user = userService.findById(id);
        boolean enabled = user.isEnabled();

        if (!enabled) {
            throw new IllegalArgumentException(NOT_CONFIRMED_EMAIL.getMessage());
        } else {
            if (referer.contains("/sign-up")) {
                return "/users/sign-in";
            } else {
                return "/users/my-page/update/user-info";
            }
        }
    }

    @Transactional(readOnly = true)
    private void validateRegistrationDto(RegistrationDto registrationDto) {
        if (userService.existsByUsername(registrationDto.getUsername())) {
            throw new CustomValidException(USERNAME, registrationDto.getUsername(), EXISTENT_USERNAME);
        }
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new CustomValidException(CONFIRM_PASSWORD, registrationDto.getConfirmPassword(), MISMATCH_CONFIRM_PASSWORD);
        }
        if (userService.existsByNickname(registrationDto.getNickname())) {
            throw new CustomValidException(NICKNAME, registrationDto.getNickname(), EXISTENT_NICKNAME);
        }
        if (userService.existsByEmail(registrationDto.getEmail())) {
            throw new CustomValidException(EMAIL, registrationDto.getEmail(), EXISTENT_EMAIL);
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
            throw new IllegalArgumentException(CONFIRMED_EMAIL.getMessage());
        } else if (LocalDateTime.now().isAfter(user.getCreatedDate().plusMinutes(12))) {
            throw new IllegalArgumentException(RESEND_NOT_ALLOWED.getMessage());
        }
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

    private void validateToken(Tokens token) {
        if (token.getConfirmedAt() != null) {
            throw new IllegalArgumentException(CONFIRMED_EMAIL.getMessage());
        }
        if (token.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(EXPIRED_TOKEN.getMessage());
        }
    }

    @Transactional
    private void updateTokenAndUser(Tokens token) {
        token.updateConfirmedAt(LocalDateTime.now());
        token.getUser().updateEnabled();
    }

    private void checkUsername(String username) {
        if (username.isBlank()) {
            throw new CustomValidException(USERNAME, username, EMPTY_USERNAME);
        }
        if (userService.existsByUsername(username)) {
            throw new CustomValidException(USERNAME, username, EXISTENT_USERNAME);
        }
    }

    private void checkNickname(String nickname) {
        if (nickname.isBlank()) {
            throw new CustomValidException(NICKNAME, nickname, EMPTY_NICKNAME);
        }
        if (userService.existsByNickname(nickname)) {
            throw new CustomValidException(NICKNAME, nickname, EXISTENT_NICKNAME);
        }
    }

    private void checkEmail(String email) {
        if (email.isBlank()) {
            throw new CustomValidException(EMAIL, email, EMPTY_EMAIL);
        }
        if (userService.existsByEmail(email)) {
            throw new CustomValidException(EMAIL, email, EXISTENT_EMAIL);
        }
    }
}
