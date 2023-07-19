package com.eskgus.nammunity.web.controller.user;

import com.eskgus.nammunity.service.user.RegistrationService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.service.user.UserUpdateService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users/confirm")
public class ConfirmationApiController {
    private final RegistrationService registrationService;
    private final UserService userService;
    private final UserUpdateService userUpdateService;

    @GetMapping
    public RedirectView confirmToken(@RequestParam String token,
                                     RedirectAttributes ra) {
        try {
            registrationService.confirmToken(token);
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return new RedirectView("/users/confirm-email");
    }

    @GetMapping("/{id}")
    public Map<String, String> checkUserEnabled(@PathVariable Long id, HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        boolean enabled = userService.findById(id).isEnabled();
        if (!enabled) {
            response.put("error", "인증되지 않은 메일입니다.");
        } else {
            if (request.getHeader("referer").contains("sign-up")) {
                response.put("OK", "/users/sign-in");
            } else {
                response.put("OK", "/users/my-page/update/user-info");
            }
        }
        return response;
    }

    @PostMapping
    public Map<String, String> resendToken(@RequestBody Map<String, Long> id) {
        Map<String, String> response = new HashMap<>();
        try {
            registrationService.resendToken(id.get("id"));
            response.put("OK", "발송 완료");
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage().contains("더 이상")) {
                userUpdateService.deleteUser(userService.findById(id.get("id")).getUsername(), null);
            }
            response.put("error", ex.getMessage());
        }
        return response;
    }
}
