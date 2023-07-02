package com.eskgus.nammunity.config.interceptor;

import com.eskgus.nammunity.service.comments.CommentsService;
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
public class CommentsAuthInterceptor implements HandlerInterceptor {
    @Autowired
    CommentsService commentsService;

    @Autowired
    UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String httpMethod = request.getMethod();

        if (httpMethod.equals("PUT") || httpMethod.equals("DELETE")) {
            try {
                Long userId = userService.findByUsername(request.getUserPrincipal().getName()).getId();

                Map<?, ?> pathVariables = (Map<?, ?>) request
                        .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
                Long id = Long.parseLong((String) pathVariables.get("id"));
                Long authorId = commentsService.findById(id).getUser().getId();

                if (!userId.equals(authorId)) {
                    response.sendError(HttpStatus.FORBIDDEN.value());
                    return false;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return true;
    }
}
