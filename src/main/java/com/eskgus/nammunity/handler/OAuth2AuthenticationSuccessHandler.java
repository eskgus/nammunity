package com.eskgus.nammunity.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;

@Log4j2
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2AuthenticationSuccessHandler.....");

        DefaultOAuth2User user = (DefaultOAuth2User) authentication.getPrincipal();

        Cookie accessToken = (Cookie) user.getAttributes().get("accessToken");
        response.addCookie(accessToken);

        Object url = request.getSession().getAttribute("prePage");
        if (url != null) {
            response.sendRedirect(url.toString());
        } else {
            response.sendRedirect("/");
        }
    }
}
