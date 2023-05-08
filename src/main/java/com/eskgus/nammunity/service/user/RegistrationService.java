package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.tokens.Tokens;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.email.EmailService;
import com.eskgus.nammunity.service.tokens.TokensService;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    public void register(RegistrationDto registrationDto) {
        if (userService.checkUsername(registrationDto.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username");
        } else if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "confirmPassword");
        } else if (userService.checkNickname(registrationDto.getNickname())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "nickname");
        } else if (userService.checkEmail(registrationDto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email");
        }

        String password = encoder.encode(registrationDto.getPassword());
        RegistrationDto encRegistrationDto = RegistrationDto.builder()
                .username(registrationDto.getUsername()).password(password)
                .nickname(registrationDto.getNickname())
                .email(registrationDto.getEmail())
                .role(Role.USER).build();

        Long id = userService.signUp(encRegistrationDto);
        sendToken(id);
    }

    @Transactional
    public void sendToken(Long id) {
        User user = userService.findById(id);

        String token = UUID.randomUUID().toString();
        Tokens newToken = Tokens.builder().token(token).createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(3)).user(user).build();
        tokensService.save(newToken);

        String text = emailService.setEmailText(user.getUsername(), token);
        emailService.send(user.getEmail(), text);
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
        userService.updateEnabled(confirmationToken.getUser().getEmail());
    }
}
