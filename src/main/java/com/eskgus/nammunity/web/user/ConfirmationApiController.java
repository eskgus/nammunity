package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.service.user.RegistrationService;
import com.eskgus.nammunity.service.user.UserService;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users/confirm")
public class ConfirmationApiController {
    private final RegistrationService registrationService;
    private final UserService userService;

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
    public String checkUserEnabled(@PathVariable Long id) {
        boolean enabled = userService.findById(id).isEnabled();
        if (!enabled) {
            return "인증되지 않은 이메일입니다.";
        }
        return "OK";
    }

    @PostMapping
    public Map<String, String> resendToken(@RequestParam(name = "id") Long id,
                                           @RequestParam(required = false, name = "email")
                              @Email(message = "이메일 형식이 맞지 않습니다.") String email) {
        Map<String, String> response = new HashMap<>();
        try {
            response = registrationService.resendToken(id, email);
        } catch (IllegalArgumentException ex) {
            response.put("error", ex.getMessage());
        }
        return response;
    }
}
