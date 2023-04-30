package com.eskgus.nammunity.config.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserAuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String httpMethod = request.getMethod();

        if (httpMethod.equals("GET")) {
            String url = request.getHeader("referer");
            if (url != null && !url.contains("/sign-in") && !url.contains("/sign-up")) {
                request.getSession().setAttribute("prePage", url);
            }
        }
        return true;
    }
}
