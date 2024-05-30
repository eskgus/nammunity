package com.eskgus.nammunity.config.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.stream.Stream;

@Component
public class UserAuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String httpMethod = request.getMethod();

        if (httpMethod.equals(HttpMethod.GET.name())) {
            String referer = request.getHeader("referer");
            Object prePage = request.getSession().getAttribute("prePage");

            if (prePage == null && isRefererValid(referer)) {
                request.getSession().setAttribute("prePage", referer);
            }
        }
        return true;
    }

    private boolean isRefererValid(String referer) {
        if (referer == null) {
            return false;
        }
        return Stream.of("/sign-in", "/sign-up", "/find", "/confirm").noneMatch(referer::contains);
    }
}
