package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SignInService {
    private final UserService userService;

    @Transactional
    public Integer increaseAttempt(String username) {
        User user = userService.findByUsername(username);
        int attempt = user.increaseAttempt();
        if (attempt >= 5 && !user.isLocked()) {
            user.updateLocked();
        }
        return attempt;
    }

    @Transactional
    public void resetAttempt(User user) {
        user.resetAttempt();
    }

    public String findUsername(String email) {
        User user = userService.findByEmail(email);
        return user.getUsername();
    }
}
