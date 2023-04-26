package com.eskgus.nammunity.advice;

import com.eskgus.nammunity.domain.user.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class TemplateAdvice {
    @ModelAttribute
    public void addDefaultAttributes(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        if (user != null) {
            model.addAttribute("user", user.getNickname());
        }
    }
}
