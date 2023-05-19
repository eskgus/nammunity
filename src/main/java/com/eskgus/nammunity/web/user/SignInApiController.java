package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.service.user.SignInService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users/sign-in")
public class SignInApiController {
    private final SignInService signInService;

    @GetMapping
    public Map<String, String> findUsername(@RequestParam String email) {
        Map<String, String> response = new HashMap<>();
        try {
            if (email.isBlank()) {
                throw new IllegalArgumentException("이메일을 입력하세요.");
            }
            String username = signInService.findUsername(email);
            response.put("OK", "가입하신 ID는 " + username.substring(0, 3) + "****입니다.");
        } catch (IllegalArgumentException ex) {
            response.put("error", ex.getMessage());
        }
        return response;
    }

    @PutMapping
    public Map<String, String> findPassword(@RequestParam String username) {
        Map<String, String> response = new HashMap<>();
        try {
            if (username.isBlank()) {
                throw new IllegalArgumentException("ID를 입력하세요.");
            }
            signInService.findPassword(username);
            response.put("OK", "가입된 이메일로 임시 비밀번호를 보냈습니다.");
        } catch (IllegalArgumentException ex) {
            response.put("error", ex.getMessage());
        }
        return response;
    }
}
