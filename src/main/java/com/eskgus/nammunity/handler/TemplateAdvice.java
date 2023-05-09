package com.eskgus.nammunity.handler;

import com.eskgus.nammunity.domain.user.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class TemplateAdvice {
    @ModelAttribute
    public void addDefaultAttributes(@AuthenticationPrincipal CustomUserDetails user, Model model,
                                     HttpServletRequest request) {
        Map<String, String> attr = new HashMap<>();

        if (user != null) {
            attr.put("auth", user.getNickname());
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
