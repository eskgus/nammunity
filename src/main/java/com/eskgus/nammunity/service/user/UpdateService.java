package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.user.PasswordUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UpdateService {
    private final BCryptPasswordEncoder encoder;
    private final UserRepository userRepository;

    @Transactional
    public void updatePassword(PasswordUpdateDto requestDto, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new
                IllegalArgumentException("username"));

        String oldPassword = requestDto.getOldPassword();
        String currentPassword = user.getPassword();
        String newPassword = requestDto.getPassword();

        if (!encoder.matches(oldPassword, currentPassword)) {
            throw new IllegalArgumentException("oldPassword");
        } else if (oldPassword.equals(newPassword)) {
            throw new IllegalArgumentException("password");
        } else if (!newPassword.equals(requestDto.getConfirmPassword())) {
            throw new IllegalArgumentException("confirmPassword");
        }

        user.updatePassword(encoder.encode(newPassword));
    }

    @Transactional
    public void updateEmail(User user, String email) {
        user.updateEmail(email);
    }
}
