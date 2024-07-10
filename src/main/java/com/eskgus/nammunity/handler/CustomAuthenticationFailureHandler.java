package com.eskgus.nammunity.handler;

import com.eskgus.nammunity.service.user.SignInService;
import com.eskgus.nammunity.util.ResponseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private final SignInService signInService;

    @Autowired
    public CustomAuthenticationFailureHandler(SignInService signInService) {
        this.signInService = signInService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        if (doesUserExist(exception)) {
            increaseAttempt(request);
        }

        ResponseUtil.setMessage(exception, request, response);

        setDefaultFailureUrl("/users/sign-in?error");
        super.onAuthenticationFailure(request, response, exception);
    }

    private boolean doesUserExist(AuthenticationException ex) {
        return !(ex instanceof UsernameNotFoundException) && !(ex instanceof AuthenticationServiceException);
    }

    private void increaseAttempt(HttpServletRequest request) {
        String username = request.getParameter("username");
        signInService.increaseAttempt(username);
    }
}
