package com.eskgus.nammunity.config.interceptor;

import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.comments.CommentsSearchService;
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
    CommentsSearchService commentsSearchService;

    @Autowired
    PrincipalHelper principalHelper;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String httpMethod = request.getMethod();

        if (httpMethod.equals("PUT") || httpMethod.equals("DELETE")) {
            try {
                User user = principalHelper.getUserFromPrincipal(request.getUserPrincipal(), true);

                // http method가 delete고, user의 role이 admin이면 통과
                if (httpMethod.equals("DELETE") && user.getRole().equals(Role.ADMIN)) {
                    return true;
                }

                // 아니면 작성자랑 user가 같은지 확인
                Long userId = user.getId();

                Map<?, ?> pathVariables = (Map<?, ?>) request
                        .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
                Long id = Long.parseLong((String) pathVariables.get("id"));
                Long authorId = commentsSearchService.findById(id).getUser().getId();

                if (!userId.equals(authorId)) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType("application/json; charset=UTF-8");
                    response.getWriter().write("권한이 없습니다.");
                    return false;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return true;
    }
}
