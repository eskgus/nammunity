package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.BANNED;

@RequiredArgsConstructor
@Service
public class SignInService {
    private final UserService userService;
    private final BannedUsersService bannedUsersService;
    private final UserUpdateService userUpdateService;
    private final EmailService emailService;

    @Transactional
    public Integer increaseAttempt(String username) {
        User user = findUsersByUsername(username);
        int attempt = user.increaseAttempt();
        if (attempt == 5 && !user.isLocked()) {
            user.updateLocked();
        }
        return attempt;
    }

    @Transactional
    public void resetAttempt(User user) {
        user.resetAttempt();
    }

    @Transactional(readOnly = true)
    public String findUsername(String email) {
        User user = userService.findByEmail(email);

        String encryptedUsername = encryptUsername(user.getUsername());
        return "가입하신 ID는 " + encryptedUsername + "입니다.";
    }

    @Transactional
    public void findPassword(String username) {
        User user = findUsersByUsername(username);

        if (!bannedUsersService.isAccountNonBanned(username)) {
            throw new IllegalArgumentException(BANNED.getMessage());
        }

        createAndUpdatePassword(user);
        updateUserLockedIfIsLocked(user);
    }

    private String encryptUsername(String username) {
        if (username.charAt(1) == '_') {    // 소셜 회원가입 사용자
            return username;
        }
        return username.substring(0, 3) + "****";
    }

    private User findUsersByUsername(String username) {
        return userService.findByUsername(username);
    }

    private void createAndUpdatePassword(User user) {
        String randomPassword = createRandomPassword();
        sendRandomPasswordEmail(user, randomPassword);

        userUpdateService.encryptAndUpdatePassword(user, randomPassword);
    }

    private void updateUserLockedIfIsLocked(User user) {
        if (user.isLocked()) {
            user.updateLocked();
        }
    }

    private String createRandomPassword() {
        char[] chars = new char[36];
        for (int i = 0; i < 36; i++) {
            if (i < 10) {   // 0 to 9
                chars[i] = (char) (i + 48);
            } else {    // A to Z
                chars[i] = (char) (i + 55);
            }
        }

        StringBuilder passwordBuilder = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            int index = (int) (chars.length * Math.random());
            passwordBuilder.append(chars[index]);
        }

        return passwordBuilder.toString();
    }

    private void sendRandomPasswordEmail(User user, String randomPassword) {
        String email = user.getEmail();
        String text = emailService.setRandomPasswordEmailText(randomPassword);
        emailService.send(email, text);
    }
}
