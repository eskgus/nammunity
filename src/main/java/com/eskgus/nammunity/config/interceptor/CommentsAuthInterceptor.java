package com.eskgus.nammunity.config.interceptor;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.comments.CommentsSearchService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Component
public class CommentsAuthInterceptor implements HandlerInterceptor {
    @Autowired
    CommentsSearchService commentsSearchService;

    @Autowired
    PrincipalHelper principalHelper;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String httpMethod = request.getMethod();
        if (isHttpMethodPutOrDelete(httpMethod)) {
            User user = principalHelper.getUserFromPrincipal(request.getUserPrincipal(), true);

            if (httpMethod.equals(HttpMethod.DELETE.name()) && user.getRole().equals(Role.ADMIN)) {
                return true;
            }
            return doesUserWriteComment(user, request, response);
        }
        return true;
    }

    private boolean isHttpMethodPutOrDelete(String httpMethod) {
        return httpMethod.equals(HttpMethod.PUT.name()) || httpMethod.equals(HttpMethod.DELETE.name());
    }

    private boolean doesUserWriteComment(User user, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long userId = user.getId();
        Long authorId = getAuthorIdFromRequest(request);

        if (!userId.equals(authorId)) {
            principalHelper.denyAccess(request, response);
            return false;
        }
        return true;
    }

    private Long getAuthorIdFromRequest(HttpServletRequest request) {
        Map<?, ?> pathVariables = (Map<?, ?>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        Long commentId = Long.parseLong((String) pathVariables.get("id"));
        Comments comment = commentsSearchService.findById(commentId);
        User author = comment.getUser();
        return author.getId();
    }
}
