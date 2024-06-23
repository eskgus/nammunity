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

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;

@Component
public class PrincipalHelper {
    private final UserService userService;

    @Autowired
    public PrincipalHelper(UserService userService) {
        this.userService = userService;
    }

    public User getUserFromPrincipal(Principal principal, boolean throwExceptionOnMissingPrincipal) {
        if (principal != null) {
            return userService.findByUsername(principal.getName());
        }
        if (throwExceptionOnMissingPrincipal) {
            throw new IllegalArgumentException(UNAUTHORIZED.getMessage());
        }
        return null;
    }

    public void denyAccess(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (request.getRequestURI().startsWith("/api")) {
            sendResponse(response);
        } else {
            throw new AccessDeniedException(FORBIDDEN.getMessage());
        }
    }

    private void sendResponse(HttpServletResponse response) throws Exception {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(FORBIDDEN.getMessage());
    }
}
