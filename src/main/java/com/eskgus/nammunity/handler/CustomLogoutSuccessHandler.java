package com.eskgus.nammunity.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import java.io.IOException;

public class CustomLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {
    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication)
            throws IOException, ServletException {
        Object referer = request.getHeader("referer");
        String url = (referer != null && !referer.toString().contains("/delete/account")) ? referer.toString() : "/";
        response.sendRedirect(url);
    }
}
