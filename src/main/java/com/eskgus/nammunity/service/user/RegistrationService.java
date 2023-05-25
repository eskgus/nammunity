package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.tokens.Tokens;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
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
    private final UserRepository userRepository;

    @Transactional
    public Long register(RegistrationDto registrationDto) {
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new IllegalArgumentException("username");
        } else if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new IllegalArgumentException("confirmPassword");
        } else if (userRepository.existsByNickname(registrationDto.getNickname())) {
            throw new IllegalArgumentException("nickname");
        } else if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("email");
        }

        String password = encoder.encode(registrationDto.getPassword());
        RegistrationDto encRegistrationDto = RegistrationDto.builder()
                .username(registrationDto.getUsername()).password(password)
                .nickname(registrationDto.getNickname())
                .email(registrationDto.getEmail())
                .role(Role.USER).build();

        Long id = userService.signUp(encRegistrationDto);
        sendToken(id, encRegistrationDto.getEmail());
        return id;
    }

    @Transactional
    public void sendToken(Long id, String email) {
        User user = userService.findById(id);

        String token = UUID.randomUUID().toString();
        Tokens newToken = Tokens.builder().token(token).createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(3)).user(user).build();
        tokensService.save(newToken);

        String text;
        if (!email.equals(user.getEmail())) {
            text = emailService.setEmailText("", token);
        } else {
            text = emailService.setEmailText(user.getUsername(), token);
        }
        emailService.send(email, text);
    }

    @Transactional
    public void confirmToken(String token) {
        Tokens confirmationToken = tokensService.findByToken(token).orElseThrow(() -> new
                IllegalArgumentException("인증 링크가 존재하지 않습니다."));
        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalArgumentException("이미 인증된 메일입니다.");
        }
        if (confirmationToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("인증 링크가 만료됐습니다.");
        }

        tokensService.updateConfirmedAt(token, LocalDateTime.now());
        userService.updateEnabled(confirmationToken.getUser());
    }

    public boolean check(String type, String value) {
        if (type.equals("username")) {
            return userRepository.existsByUsername(value);
        } else if (type.equals("nickname")) {
            return userRepository.existsByNickname(value);
        }
        return userRepository.existsByEmail(value);
    }

    @Transactional
    public void resendToken(Long id) {
        User user = userService.findById(id);

        if (user.isEnabled()) {
            throw new IllegalArgumentException("이미 인증된 메일입니다.");
        } else if (LocalDateTime.now().isAfter(user.getCreatedDate().plusMinutes(12))) {
            tokensService.deleteAllByUser(user);
            userService.delete(user);
            throw new IllegalArgumentException("더 이상 재발송할 수 없어요. 다시 가입해 주세요.");
        }

        tokensService.updateExpiredAtAllByUser(user, LocalDateTime.now());
        sendToken(id, user.getEmail());
    }
}
