package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public Long signUp(RegistrationDto registrationDto) {
        return userRepository.save(registrationDto.toEntity()).getId();
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

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    @Transactional
    public void delete(User user) {
        userRepository.delete(user);
    }
}
