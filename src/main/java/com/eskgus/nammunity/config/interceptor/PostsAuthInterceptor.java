package com.eskgus.nammunity.config.interceptor;

import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Component
public class PostsAuthInterceptor implements HandlerInterceptor {
    @Autowired
    PostsService postsService;

    @Autowired
    UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String httpMethod = request.getMethod();

        if (httpMethod.equals("GET") || httpMethod.equals("PUT") || httpMethod.equals("DELETE")) {
            String username=request.getUserPrincipal().getName();
            Long userId = userService.findByUsername(username).getId();

            Map<?, ?> pathVariables = (Map<?, ?>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            Long id = Long.parseLong((String)pathVariables.get("id"));
            Long authorId = postsService.findById(id).getUserId();

            if (!userId.equals(authorId)) {
                response.sendError(HttpStatus.FORBIDDEN.value());
                return false;
            }
        }
        return true;
    }
}
