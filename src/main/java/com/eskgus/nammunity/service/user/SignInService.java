package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SignInService {
    private final EmailService emailService;
    private final BCryptPasswordEncoder encoder;
    private final UserRepository userRepository;
    private final BannedUsersService bannedUsersService;

    @Transactional
    public Integer increaseAttempt(User user) {
        int attempt = user.increaseAttempt();
        if (attempt == 5 && !user.isLocked()) {
            user.updateLocked();
        }
        return attempt;
    }

    @Transactional(readOnly = true)
    public String findUsername(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new
                IllegalArgumentException("가입되지 않은 이메일입니다."));

        String username = user.getUsername();
        if (username.charAt(1) == '_') {
            return username;
        }
        return username.substring(0, 3) + "****";
    }

    @Transactional
    public void findPassword(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new
                IllegalArgumentException("존재하지 않는 ID입니다."));

        // 활동 정지된 계정이면 예외 메시지 던지기
        if (user.isLocked() && bannedUsersService.existsByUser(user)) {
            throw new IllegalArgumentException("활동 정지된 계정입니다. 자세한 내용은 메일을 확인하세요.");
        }

        char[] ch = new char[36];
        for (int i = 0; i < 36; i++) {
            if (i < 10) {
                ch[i] = (char) (i + 48);
            } else {
                ch[i] = (char) (i + 55);
            }
        }

        String str = "";
        for (int i = 0; i < 10; i++) {
            int idx = (int) (ch.length * Math.random());
            str += ch[idx];
        }

        String text = emailService.setEmailText(str);
        emailService.send(user.getEmail(), text);

        String password = encoder.encode(str);
        user.updatePassword(password);

        if (user.isLocked()) {
            user.updateLocked();
        }
    }
}
