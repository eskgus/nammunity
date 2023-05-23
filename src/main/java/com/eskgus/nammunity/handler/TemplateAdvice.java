package com.eskgus.nammunity.handler;

import com.eskgus.nammunity.domain.user.CustomUserDetails;
import com.eskgus.nammunity.service.user.CustomUserDetailsService;
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
    private final CustomUserDetailsService userDetailsService;

    @ModelAttribute
    public void addDefaultAttributes(Principal principal, Model model, HttpServletRequest request) {
        Map<String, String> attr = new HashMap<>();

        if (principal != null) {
            String nickname = ((CustomUserDetails)
                    userDetailsService.loadUserByUsername(principal.getName())).getNickname();
            attr.put("auth", nickname);
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
