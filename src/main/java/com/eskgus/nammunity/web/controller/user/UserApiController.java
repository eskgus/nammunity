package com.eskgus.nammunity.web.controller.user;

import com.eskgus.nammunity.service.user.CustomOAuth2UserService;
import com.eskgus.nammunity.service.user.RegistrationService;
import com.eskgus.nammunity.service.user.UserUpdateService;
import com.eskgus.nammunity.web.dto.user.EmailUpdateDto;
import com.eskgus.nammunity.web.dto.user.NicknameUpdateDto;
import com.eskgus.nammunity.web.dto.user.PasswordUpdateDto;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserApiController {
    private final RegistrationService registrationService;
    private final UserUpdateService userUpdateService;
    private final CustomOAuth2UserService customOAuth2UserService;

    @PostMapping
    public Map<String, String> signUp(@Valid @RequestBody RegistrationDto registrationDto) {
        Map<String, String> response = new HashMap<>();
        try {
            response.put("OK", registrationService.register(registrationDto).toString());
        } catch (IllegalArgumentException ex) {
            String reason = ex.getMessage();
            response.put(reason, switch (reason) {
                case "username" -> "이미 사용 중인 ID입니다.";
                case "confirmPassword" -> "비밀번호가 일치하지 않습니다.";
                case "nickname" -> "이미 사용 중인 닉네임입니다.";
                case "email" -> "이미 사용 중인 이메일입니다.";
                default -> throw new IllegalStateException("unexpected value");
            });
        }
        return response;
    }

    @GetMapping
    public String check(@RequestParam(name = "username", required = false) String username,
                        @RequestParam(name = "nickname", required = false) String nickname,
                        @RequestParam(name = "email", required = false) String email) {
        if (username != null) {
            if (username.isBlank()) {
                return "ID를 입력하세요.";
            } else if (registrationService.check("username", username)) {
                return "이미 사용 중인 ID입니다.";
            }
        } else if (nickname != null) {
            if (nickname.isBlank()) {
                return "닉네임을 입력하세요.";
            } else if (registrationService.check("nickname", nickname)) {
                return "이미 사용 중인 닉네임입니다.";
            }
        } else if (email != null) {
            if (email.isBlank()) {
                return "이메일을 입력하세요.";
            } else if (registrationService.check("email", email)) {
                return "이미 사용 중인 이메일입니다.";
            }
        }
        return "OK";
    }

    @PutMapping("/update/password")
    public Map<String, String> updatePassword(@Valid @RequestBody PasswordUpdateDto requestDto,
                                 Principal principal) {
        Map<String, String> response = new HashMap<>();
        String username = principal.getName();
        try {
            userUpdateService.updatePassword(requestDto, username);
            response.put("OK", "비밀번호가 변경됐습니다.");
        } catch (IllegalArgumentException ex) {
            String reason = ex.getMessage();
            response.put(reason, switch(reason) {
                case "username" -> "존재하지 않는 ID입니다.";
                case "oldPassword" -> "현재 비밀번호가 일치하지 않습니다.";
                case "password" -> "현재 비밀번호와 새 비밀번호가 같으면 안 됩니다.";
                case "confirmPassword" -> "비밀번호가 일치하지 않습니다.";
                default -> throw new IllegalStateException("unexpected value");
            });
        }
        return response;
    }

    @PutMapping("/update/nickname")
    public Map<String, String> updateNickname(@Valid @RequestBody NicknameUpdateDto requestDto,
                                              Principal principal) {
        Map<String, String> response = new HashMap<>();
        String username = principal.getName();
        try {
            userUpdateService.updateNickname(requestDto, username);
            response.put("OK", "수정 완료");
        } catch (IllegalArgumentException ex) {
            response.put("error", ex.getMessage());
        }
        return response;
    }

    @PutMapping("/update/email")
    public Map<String, String> updateEmail(@Valid @RequestBody EmailUpdateDto requestDto,
                                           Principal principal) {
        Map<String, String> response = new HashMap<>();
        String username = principal.getName();
        try {
            userUpdateService.updateEmail(requestDto, username);
            response.put("OK", "발송 완료");
        } catch (IllegalArgumentException ex) {
            response.put("error", ex.getMessage());
        }
        return response;
    }

    @DeleteMapping("/delete")
    public Map<String, String> deleteUser(Principal principal,
                                          @CookieValue(name = "access_token", required = false) String accessToken,
                                          HttpServletResponse servletResponse) {
        Map<String, String> response = new HashMap<>();
        String username = principal.getName();
        try {
            Cookie cookie = userUpdateService.deleteUser(username, accessToken);
            if (cookie != null) {
                servletResponse.addCookie(cookie);
            }
            response.put("OK", "탈퇴됐습니다.");
        } catch (IllegalArgumentException ex) {
            response.put("error", ex.getMessage());
        }
        return response;
    }

    @PostMapping("/unlink/{social}")
    public Map<String, String> unlinkSocial(@PathVariable String social,
                                            @CookieValue(name = "access_token", required = false) String accessToken,
                                            Principal principal, HttpServletResponse servletResponse) {
        Map<String, String> response = new HashMap<>();
        String username = principal.getName();

        try {
            Cookie cookie = customOAuth2UserService.unlinkSocial(username, social, accessToken);
            servletResponse.addCookie(cookie);
            response.put("OK", "연동 해제 완료 !");
        } catch (HttpClientErrorException ex) {
            log.info(ex.getMessage());
            log.info(ex.getResponseBodyAsString());
            response.put("error", "error");
        }

        return response;
    }
}
