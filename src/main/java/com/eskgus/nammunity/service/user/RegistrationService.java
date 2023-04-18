package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.tokens.Tokens;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.service.email.EmailSender;
import com.eskgus.nammunity.service.tokens.TokensService;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class RegistrationService {
    private final UserService userService;
    private final BCryptPasswordEncoder encoder;
    private final EmailSender emailSender;
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

        String token = userService.signUp(encRegistrationDto);

        String link = "http://localhost:8080/api/users/confirm?token=" + token;

        String text = setEmailText(registrationDto.getUsername(), link);

        emailSender.send(registrationDto.getEmail(), text);
    }

    public String setEmailText(String username, String link) {
        return "<div style=\"font-size: 18px; font-family: sans-serif\">" +
                "<p>안녕하세요, " + username + "님?</p>" +
                "<p>나뮤니티 가입을 환영합니다! 아래의 링크를 눌러 이메일 인증을 해주세요 ^_^</p>" +
                "<p><a href=\"" + link + "\">인증하기</a></p>" +
                "<p>링크는 5분 뒤 만료됩니다.</p></div>";
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
