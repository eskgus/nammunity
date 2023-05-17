package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.service.user.SignInService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users/sign-in")
public class SignInApiController {
    private final SignInService signInService;

    @GetMapping
    public String findUsername(@RequestParam String email) {
        String username;
        try {
            if (email.isBlank()) {
                throw new IllegalArgumentException("이메일을 입력하세요.");
            }
            username = signInService.findUsername(email);
        } catch (IllegalArgumentException ex) {
            return ex.getMessage();
        }
        return "가입하신 ID는 " + username.substring(0, 3) + "****입니다.";
    }

    @PutMapping
    public String findPassword(@RequestParam String username) {
        try {
            if (username.isBlank()) {
                throw new IllegalArgumentException("ID를 입력하세요.");
            }
            signInService.findPassword(username);
        } catch (IllegalArgumentException ex) {
            return ex.getMessage();
        }
        return "가입된 이메일로 임시 비밀번호를 보냈습니다.";
    }
}
