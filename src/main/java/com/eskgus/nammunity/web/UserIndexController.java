package com.eskgus.nammunity.web;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Controller
@RequestMapping("/users")
public class UserIndexController {
    private final UserService userService;

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
    public String signInUser(@RequestParam(required = false) Boolean error, Model model) {
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "user/sign-in";
    }

    @GetMapping("/sign-up/{id}")
    public String afterSignUp(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        return "user/after-sign-up";
    }
}
