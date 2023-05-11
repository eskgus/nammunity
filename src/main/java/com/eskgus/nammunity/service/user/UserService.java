package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.user.PasswordUpdateDto;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

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

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new
                IllegalArgumentException("가입되지 않은 이메일입니다."));
    }

    @Transactional
    public void delete(User user) {
        userRepository.delete(user);
    }

    @Transactional
    public void changePassword(PasswordUpdateDto requestDto, Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException("user not found"));

        String oldPassword = requestDto.getOldPassword();
        String currentPassword = user.getPassword();
        String newPassword = requestDto.getPassword();

        if (!encoder.matches(oldPassword, currentPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "oldPassword");
        } else if (oldPassword.equals(newPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password");
        } else if (!newPassword.equals(requestDto.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "confirmPassword");
        }

        user.updatePassword(encoder.encode(newPassword));
    }
}
