package com.eskgus.nammunity.handler;

import com.eskgus.nammunity.domain.user.CustomUserDetails;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.user.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;

public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    @Autowired
    UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {
        Long id = ((CustomUserDetails) authentication.getPrincipal()).getId();
        User user = userService.findById(id);
        if (user.getAttempt() != 0) {
            userService.resetAttempt(id);
        }

        Object url = request.getSession().getAttribute("prePage");
        if (url != null) {
            response.sendRedirect(url.toString());
        } else {
            response.sendRedirect("/");
        }
    }
}
