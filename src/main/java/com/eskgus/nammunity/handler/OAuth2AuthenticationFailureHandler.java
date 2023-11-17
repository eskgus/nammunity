package com.eskgus.nammunity.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import java.io.IOException;

@Log4j2
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        log.info("onAuthenticationFailure.....");

        String message = exception.getMessage();
        log.info("message: " + message);

        String url;
        if (message.contains("활동 정지") || message.contains("5번 이상 실패")) {
            url = "/users/sign-in?error";

            // 마이 페이지에서 소셜 연동 시 locked/banned에 걸려서 던져진 예외일 때는 로그아웃도 해줘야 함 !
            LogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.logout(request, response, null);
        } else {
            Object prePage = request.getSession().getAttribute("prePage");
            url = (prePage != null) ? prePage.toString() : "/";
        }

        FlashMap flashMap = new FlashMap();
        flashMap.put("message", message);
        FlashMapManager flashMapManager = new SessionFlashMapManager();
        flashMapManager.saveOutputFlashMap(flashMap, request, response);

        setDefaultFailureUrl(url);
        super.onAuthenticationFailure(request, response, exception);
    }
}
