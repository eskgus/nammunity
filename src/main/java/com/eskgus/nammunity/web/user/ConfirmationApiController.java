package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.tokens.TokensService;
import com.eskgus.nammunity.service.user.RegistrationService;
import com.eskgus.nammunity.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users/confirm")
public class ConfirmationApiController {
    private final RegistrationService registrationService;
    private final UserService userService;
    private final TokensService tokensService;

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

    @PostMapping("/{id}")
    public String resendToken(@PathVariable Long id,
                              @RequestParam(required = false) String email) {
        User user = userService.findById(id);
        if (email == null) {
            if (user.isEnabled()) {
                return "이미 인증된 메일입니다.";
            } else if (LocalDateTime.now().isAfter(user.getCreatedDate().plusMinutes(12))) {
                tokensService.deleteAllByUser(user);
                userService.delete(user);
                return "더 이상 재발송할 수 없어요. 다시 가입해 주세요.";
            }
            email = user.getEmail();
        } else if (email.isBlank()) {
            return "이메일을 입력하세요.";
        } else if (user.isEnabled()) {
            userService.updateEnabled(user);
        }

        registrationService.sendToken(id, email);
        return "발송 완료";
    }
}
