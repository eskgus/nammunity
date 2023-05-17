package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.service.user.RegistrationService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.user.PasswordUpdateDto;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserApiController {
    private final UserService userService;
    private final RegistrationService registrationService;

    @PostMapping
    public Map<String, String> signUp(@Valid @RequestBody RegistrationDto registrationDto) {
        Map<String, String> response = new HashMap<>();
        try {
            response.put("id", registrationService.register(registrationDto).toString());
        } catch (IllegalArgumentException ex) {
            String reason = ex.getMessage();
            switch (reason) {
                case "username":
                    response.put(reason, "이미 사용 중인 ID입니다.");
                    break;
                case "confirmPassword":
                    response.put(reason, "비밀번호가 일치하지 않습니다.");
                    break;
                case "nickname":
                    response.put(reason, "이미 사용 중인 닉네임입니다.");
                    break;
                case "email":
                    response.put(reason, "이미 사용 중인 이메일입니다.");
                    break;
            }
        }
        return response;
    }

    @GetMapping
    public String check(@RequestParam(name = "username", required = false) String username,
                        @RequestParam(name = "nickname", required = false) String nickname,
                        @RequestParam(name = "email", required = false) String email) {
        if (username != null && !username.isBlank()) {
            return userService.checkUsername(username).toString();
        } else if (nickname != null && !nickname.isBlank()) {
            return userService.checkNickname(nickname).toString();
        } else if (email != null && !email.isBlank()) {
            return userService.checkEmail(email).toString();
        }
        return "blank";
    }

    @PutMapping("/change/password")
    public Map<String, String> changePassword(@Valid @RequestBody PasswordUpdateDto requestDto,
                                 Principal principal) {
        Map<String, String> response = new HashMap<>();
        String username = principal.getName();
        try {
            userService.changePassword(requestDto, username);
            response.put("OK", "비밀번호가 변경됐습니다.");
        } catch (IllegalArgumentException ex) {
            String reason = ex.getMessage();
            switch (reason) {
                case "username":
                    response.put(reason, "존재하지 않는 ID입니다.");
                    break;
                case "oldPassword":
                    response.put(reason, "비밀번호가 틀렸습니다.");
                    break;
                case "password":
                    response.put(reason, "현재 비밀번호와 새 비밀번호가 같으면 안 됩니다.");
                    break;
                case "confirmPassword":
                    response.put(reason, "비밀번호가 일치하지 않습니다.");
                    break;
            }
        }
        return response;
    }
}
