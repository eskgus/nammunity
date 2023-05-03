package com.eskgus.nammunity.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import java.io.IOException;

public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
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
            message = "ID가 존재하지 않거나 비밀번호가 일치하지 않습니다.";
        }

        FlashMap flashMap = new FlashMap();
        flashMap.put("message", message);
        FlashMapManager flashMapManager = new SessionFlashMapManager();
        flashMapManager.saveOutputFlashMap(flashMap, request, response);

        setDefaultFailureUrl("/users/sign-in?error=true");
        super.onAuthenticationFailure(request, response, exception);
    }
}
