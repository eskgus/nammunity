package com.eskgus.nammunity.handler;

import com.eskgus.nammunity.util.ResponseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        addCookie(authentication, response);

        ResponseUtil.sendRedirect(request, response);
    }

    private void addCookie(Authentication authentication, HttpServletResponse response) {
        DefaultOAuth2User user = (DefaultOAuth2User) authentication.getPrincipal();

        Cookie accessToken = (Cookie) user.getAttributes().get("accessToken");
        response.addCookie(accessToken);
    }
}
