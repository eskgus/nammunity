package com.eskgus.nammunity.web;

import com.eskgus.nammunity.domain.user.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UserIndexController {
    @GetMapping("/sign-up")
    public String signUpUser() {
        return "user/sign-up";
    }

    @GetMapping("/confirm-email")
    public String confirmEmail(@ModelAttribute("error") String attr, Model model) {
        if (attr.isBlank()) {
            model.addAttribute("success", "이메일 인증이 완료됐습니다.");
        } else {
            model.addAttribute("error", attr);
        }
        return "user/confirm-email";
    }

    @GetMapping("/sign-in")
    public String signInUser(@RequestParam(required = false) Boolean error,
                             @AuthenticationPrincipal CustomUserDetails user, Model model) {
        if (user != null) {
            model.addAttribute("user", user.getNickname());
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "user/sign-in";
    }
}
