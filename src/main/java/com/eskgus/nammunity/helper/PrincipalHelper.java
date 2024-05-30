package com.eskgus.nammunity.helper;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class PrincipalHelper {
    @Autowired
    private UserService userService;

    public User getUserFromPrincipal(Principal principal, boolean throwExceptionOnMissingPrincipal) {
        if (principal != null) {
            return userService.findByUsername(principal.getName());
        }
        if (throwExceptionOnMissingPrincipal) {
            throw new IllegalArgumentException("로그인하세요.");
        }
        return null;
    }

    public void denyAccess(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final String FORBIDDEN_MESSAGE = "권한이 없습니다.";

        if (request.getRequestURI().startsWith("/api")) {
            sendResponse(response, FORBIDDEN_MESSAGE);
        } else {
            throw new AccessDeniedException(FORBIDDEN_MESSAGE);
        }
    }

    private void sendResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(message);
    }
}
