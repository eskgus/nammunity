package com.eskgus.nammunity.handler;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.user.SignInService;
import com.eskgus.nammunity.util.ResponseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private final PrincipalHelper principalHelper;
    private final SignInService signInService;

    @Autowired
    public CustomAuthenticationSuccessHandler(PrincipalHelper principalHelper, SignInService signInService) {
        this.principalHelper = principalHelper;
        this.signInService = signInService;
    }

    @Transactional
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {
        resetAttempt(authentication);

        ResponseUtil.sendRedirect(request, response);
    }

    private void resetAttempt(Authentication authentication) {
        User user = principalHelper.getUserFromPrincipal(authentication, true);

        if (user.getAttempt() != 0) {
            signInService.resetAttempt(user);
        }
    }
}
