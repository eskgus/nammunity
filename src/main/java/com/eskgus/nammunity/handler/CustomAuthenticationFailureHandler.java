package com.eskgus.nammunity.handler;

import com.eskgus.nammunity.service.user.SignInService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;

import static com.eskgus.nammunity.util.ResponseUtil.setMessage;

public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Autowired
    SignInService signInService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        if (doesUserExist(exception)) {
            increaseAttempt(request);
        }

        setMessage(exception, request, response);

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
