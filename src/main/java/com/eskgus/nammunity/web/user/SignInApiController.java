package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.service.user.SignInService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users/sign-in")
public class SignInApiController {
    private final SignInService signInService;

    @GetMapping
    public Map<String, String> findUsername(@RequestParam @NotBlank(message = "이메일을 입력하세요.")
                                                @Email(message = "이메일 형식이 맞지 않습니다.") String email) {
        Map<String, String> response = new HashMap<>();
        try {
            String username = signInService.findUsername(email);
            response.put("OK", "가입하신 ID는 " + username.substring(0, 3) + "****입니다.");
        } catch (IllegalArgumentException ex) {
            response.put("error", ex.getMessage());
        }
        return response;
    }

    @PutMapping
    public Map<String, String> findPassword(@RequestParam @NotBlank(message = "ID를 입력하세요.") String username,
                                            HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        try {
            signInService.findPassword(username);
            request.getSession().setAttribute("prePage", "/users/my-page/update/password");
            response.put("OK", "가입된 이메일로 임시 비밀번호를 보냈습니다.");
        } catch (IllegalArgumentException ex) {
            response.put("error", ex.getMessage());
        }
        return response;
    }
}
