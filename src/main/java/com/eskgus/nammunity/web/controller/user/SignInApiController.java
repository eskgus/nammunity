package com.eskgus.nammunity.web.controller.user;

import com.eskgus.nammunity.service.user.SignInService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users/sign-in")
public class SignInApiController {
    private final SignInService signInService;

    @GetMapping("/username")
    public ResponseEntity<String> findUsername(@RequestParam @NotBlank(message = "이메일을 입력하세요.")
                                                @Email(message = "이메일 형식이 맞지 않습니다.") String email) {
        String username = signInService.findUsername(email);
        return ResponseEntity.status(HttpStatus.OK).body(username);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> findPassword(@RequestParam @NotBlank(message = "ID를 입력하세요.") String username,
                                            HttpServletRequest request) {
        signInService.findPassword(username);
        request.getSession().setAttribute("prePage", "/users/my-page/update/password");
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
