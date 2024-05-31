package com.eskgus.nammunity.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.io.IOException;

import static com.eskgus.nammunity.util.ResponseUtil.setMessage;

public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String url = "/users/sign-in?error";

        signOut(request, response);

        setMessage(exception, request, response);

        setDefaultFailureUrl(url);
        super.onAuthenticationFailure(request, response, exception);
    }

    private void signOut(HttpServletRequest request, HttpServletResponse response) {
        // 마이 페이지에서 소셜 연동 시 locked/banned에 걸려서 던져진 예외일 때는 로그아웃도 해줘야 함 !
        LogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(request, response, null);
    }
}
