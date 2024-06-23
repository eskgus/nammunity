package com.eskgus.nammunity.web.controller.api.user;

import com.eskgus.nammunity.service.user.RegistrationService;
import com.eskgus.nammunity.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.RESEND_NOT_ALLOWED;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class ConfirmationApiController {
    private final RegistrationService registrationService;
    private final UserService userService;

    @GetMapping("/confirm")
    public RedirectView confirmToken(@RequestParam String token,
                                     RedirectAttributes ra) {
        try {
            registrationService.confirmToken(token);
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return new RedirectView("/users/confirm-email");
    }

    @GetMapping("/{id}/confirm")
    public ResponseEntity<String> checkUserEnabled(@PathVariable Long id, HttpServletRequest request) {
        String redirectUrl = registrationService.checkUserEnabled(id, request.getHeader("referer"));
        return ResponseEntity.status(HttpStatus.OK).body(redirectUrl);
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> resendToken(@RequestBody Long id) {
        try {
            registrationService.resendToken(id);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (IllegalArgumentException ex) {
            if (RESEND_NOT_ALLOWED.getMessage().equals(ex.getMessage())) {
                userService.delete(id); // 이거 트랜잭션 때문에 따로 처리함 !
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }
}
