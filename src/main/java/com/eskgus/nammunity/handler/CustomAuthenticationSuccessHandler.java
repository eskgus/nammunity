package com.eskgus.nammunity.handler;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.user.SignInService;
import com.eskgus.nammunity.service.user.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    @Autowired
    UserService userService;

    @Autowired
    SignInService signInService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {
        String username = request.getParameter("username").toString();
        User user = userService.findByUsername(username);
        if (user.getAttempt() != 0) {
            signInService.resetAttempt(user);
        }

        Object url = request.getSession().getAttribute("prePage");
        if (url != null) {
            response.sendRedirect(url.toString());
        } else {
            response.sendRedirect("/");
        }
    }
}
