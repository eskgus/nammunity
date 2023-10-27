package com.eskgus.nammunity.handler;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.user.BannedUsersService;
import com.eskgus.nammunity.service.user.SignInService;
import com.eskgus.nammunity.service.user.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import java.io.IOException;

public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Autowired
    SignInService signInService;

    @Autowired
    UserService userService;

    @Autowired
    BannedUsersService bannedUsersService;

    @Transactional
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String message;

        if (username.isBlank()) {
            message = "ID를 입력하세요.";
        } else if (password.isBlank()) {
            message = "비밀번호를 입력하세요.";
        } else {
            try {
                User user = userService.findByUsername(username);

                // 활동 정지된 사용자 거르기
                if (user.isLocked() && bannedUsersService.existsByUser(user)) {
                    message = "활동 정지된 계정입니다. 자세한 내용은 메일을 확인하세요.";
                } else {
                    // 활동 정지된 사용자가 아니면 로그인 시도 횟수 업데이트
                    int attempt = signInService.increaseAttempt(user);

                    // 로그인에 5번 이상 실패하면
                    if (user.isLocked() || attempt >= 5) {
                        message = "로그인에 5번 이상 실패했습니다. ID 또는 비밀번호 찾기를 하세요.";
                    } else {
                        message = "ID가 존재하지 않거나 비밀번호가 일치하지 않습니다.";
                    }
                }
            } catch (IllegalArgumentException ex) {
                message = ex.getMessage();
            }
        }

        FlashMap flashMap = new FlashMap();
        flashMap.put("message", message);
        FlashMapManager flashMapManager = new SessionFlashMapManager();
        flashMapManager.saveOutputFlashMap(flashMap, request, response);

        setDefaultFailureUrl("/users/sign-in?error");
        super.onAuthenticationFailure(request, response, exception);
    }
}
