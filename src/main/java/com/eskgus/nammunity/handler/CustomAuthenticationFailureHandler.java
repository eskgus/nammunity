package com.eskgus.nammunity.handler;

import com.eskgus.nammunity.service.user.SignInService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import java.io.IOException;

public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Autowired
    SignInService signInService;

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
            message = exception.getMessage();

            // user가 존재하면
            if (!(exception instanceof UsernameNotFoundException)) {
                // 로그인 시도 횟수 업데이트
                signInService.increaseAttempt(username);
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
