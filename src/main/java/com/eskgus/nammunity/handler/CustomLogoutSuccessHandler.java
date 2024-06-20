package com.eskgus.nammunity.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {
    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication)
            throws IOException, ServletException {
        Object referer = request.getHeader("referer");
        String url = validateReferer(referer) ? referer.toString() : "/";
        response.sendRedirect(url);
    }

    private boolean validateReferer(Object referer) {
        if (referer == null) {
            return false;
        }
        return !referer.toString().contains("/delete/account");
    }
}
