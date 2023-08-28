package com.eskgus.nammunity.config.interceptor;

import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.posts.PostsSearchService;
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
    PostsSearchService postsSearchService;

    @Autowired
    UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String httpMethod = request.getMethod();

        if (httpMethod.equals("GET") || httpMethod.equals("PUT") || httpMethod.equals("DELETE")) {
            try {
                User user = userService.findByUsername(request.getUserPrincipal().getName());
                // http method가 delete고, user의 role이 admin이면 통과
                if (httpMethod.equals("DELETE") && user.getRole().equals(Role.ADMIN)) {
                    return true;
                }

                // 아니면 작성자랑 user가 같은지 확인
                Long userId = user.getId();

                Map<?, ?> pathVariables = (Map<?, ?>) request
                        .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
                Long id = Long.parseLong((String) pathVariables.get("id"));
                Long authorId = postsSearchService.findById(id).getUser().getId();

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
