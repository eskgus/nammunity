package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.tokens.Tokens;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.service.tokens.TokensService;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final TokensService tokensService;

    @Transactional
    public String signUp(RegistrationDto registrationDto) {
        User user = userRepository.save(registrationDto.toEntity());

        String token = UUID.randomUUID().toString();
        Tokens newToken = Tokens.builder().token(token).createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(5)).user(user).build();
        tokensService.save(newToken);

        return token;
    }

    public Boolean checkUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public Boolean checkNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public Boolean checkEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public void updateEnabled(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new
                IllegalArgumentException("가입되지 않은 이메일입니다."));
        user.updateEnabled();
    }

    public User findByNickname(String nickname) {
        return userRepository.findByNickname(nickname).orElseThrow(() -> new
                IllegalArgumentException("존재하지 않는 닉네임입니다."));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new
                IllegalArgumentException("존재하지 않는 ID입니다."));
    }
}
