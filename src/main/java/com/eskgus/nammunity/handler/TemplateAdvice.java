package com.eskgus.nammunity.handler;

import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@RequiredArgsConstructor
public class TemplateAdvice {
    private final UserService userService;

    @ModelAttribute
    public void addDefaultAttributes(Principal principal, Model model, HttpServletRequest request) {
        Map<String, String> attr = new HashMap<>();

        if (principal != null) {
            try {
                User user = userService.findByUsername(principal.getName());
                String nickname = user.getNickname();
                attr.put("auth", nickname);

                if (user.getRole().equals(Role.ADMIN)) {
                    attr.put("admin", "true");
                }
            } catch (IllegalArgumentException ex) {
                attr.put("error", ex.getMessage());
                attr.put("signOut", "/users/sign-out");
            }
        }

        Object url = request.getSession().getAttribute("prePage");
        if (url != null) {
            attr.put("prePage", url.toString());
        } else {
            attr.put("prePage", "/");
        }

        model.addAllAttributes(attr);
    }
}
