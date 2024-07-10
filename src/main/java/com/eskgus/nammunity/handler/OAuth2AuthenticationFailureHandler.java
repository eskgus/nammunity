package com.eskgus.nammunity.handler;

import com.eskgus.nammunity.util.ResponseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String url = setUrl(exception, request, response);

        ResponseUtil.setMessage(exception, request, response);

        setDefaultFailureUrl(url);
        super.onAuthenticationFailure(request, response, exception);
    }

    private String setUrl(AuthenticationException ex, HttpServletRequest request, HttpServletResponse response) {
        if (ex.getMessage().equals("연동할 계정을 사용 중인 다른 사용자가 있습니다.")) {
            return "/users/my-page/update/user-info";
        } else {    // locked/banned
            signOut(request, response);
            return "/users/sign-in?error";
        }
    }

    private void signOut(HttpServletRequest request, HttpServletResponse response) {
        LogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(request, response, null);
    }
}
