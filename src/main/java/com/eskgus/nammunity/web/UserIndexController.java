package com.eskgus.nammunity.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
