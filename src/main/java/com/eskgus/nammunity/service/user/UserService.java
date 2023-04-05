package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.UserRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    @Transactional
    public Long signUp(UserRequestDto requestDto) {
        String password = encoder.encode(requestDto.getPassword());
        UserRequestDto encRequestDto = UserRequestDto.builder()
                .username(requestDto.getUsername()).password(password).nickname(requestDto.getNickname()).build();
        return userRepository.save(encRequestDto.toEntity()).getId();
    }

    public Boolean checkUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public Boolean checkNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}
