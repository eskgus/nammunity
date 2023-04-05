package com.eskgus.nammunity.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserIndexController {
    @GetMapping("/user/sign-up")
    public String signUpUser() {
        return "user/sign-up";
    }
}
