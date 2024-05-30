package com.eskgus.nammunity.config.interceptor;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.posts.PostsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Component
public class PostsAuthInterceptor implements HandlerInterceptor {
    @Autowired
    PostsService postsService;

    @Autowired
    PrincipalHelper principalHelper;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String httpMethod = request.getMethod();
        if (isHttpMethodNotPost(httpMethod)) {
            User user = principalHelper.getUserFromPrincipal(request.getUserPrincipal(), true);

            if (httpMethod.equals(HttpMethod.DELETE.name()) && user.getRole().equals(Role.ADMIN)) {
                return true;
            }
            return doesUserWritePost(user, request, response);
        }
        return true;
    }

    private boolean isHttpMethodNotPost(String httpMethod) {
        return httpMethod.equals(HttpMethod.GET.name())
                || httpMethod.equals(HttpMethod.PUT.name())
                || httpMethod.equals(HttpMethod.DELETE.name());
    }

    private boolean doesUserWritePost(User user, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long userId = user.getId();
        Long authorId = getAuthorIdFromRequest(request);

        if (!userId.equals(authorId)) {
            denyAccess(request, response);
            return false;
        }
        return true;
    }

    private Long getAuthorIdFromRequest(HttpServletRequest request) {
        Map<?, ?> pathVariables = (Map<?, ?>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        Long postId = Long.parseLong((String) pathVariables.get("id"));
        Posts post = postsService.findById(postId);
        User author = post.getUser();
        return author.getId();
    }

    private void denyAccess(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final String FORBIDDEN_MESSAGE = "권한이 없습니다.";

        if (request.getRequestURI().startsWith("/api")) {
            sendResponse(response, FORBIDDEN_MESSAGE);
        } else {
            throw new AccessDeniedException(FORBIDDEN_MESSAGE);
        }
    }

    private void sendResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(message);
    }
}
