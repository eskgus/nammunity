package com.eskgus.nammunity.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        if (request.getRequestURI().startsWith("/api")) {
            sendRestResponse(response);
        } else {
            sendMvcResponse(request, response);
        }
    }

    private void sendRestResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write("로그인하세요.");
    }

    private void sendMvcResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.getSession().setAttribute("prePage", request.getRequestURL());
        response.sendRedirect("/users/sign-in");
    }
}
