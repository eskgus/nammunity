package com.eskgus.nammunity.web;

import com.eskgus.nammunity.service.user.RegistrationService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserApiController {
    private final UserService userService;
    private final RegistrationService registrationService;

    @PostMapping
    public Long signUp(@Valid @RequestBody RegistrationDto registrationDto) {
        registrationService.register(registrationDto);
        return userService.findByUsername(registrationDto.getUsername()).getId();
    }

    @GetMapping("/confirm")
    public RedirectView confirmToken(@RequestParam String token) {
        registrationService.confirmToken(token);
        return new RedirectView("/users/confirm-email");
    }

    @GetMapping("/confirm/{id}")
    public void checkUserEnabled(@PathVariable Long id) {
        boolean enabled = userService.findById(id).isEnabled();
        if (!enabled) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "confirmEmail");
        }
    }

    @PostMapping("/confirm/{id}")
    public void resendToken(@PathVariable Long id) {
        registrationService.sendEmail(id);
    }

    @GetMapping("/exists/username/{username}")
    public Boolean checkUsername(@PathVariable String username) {
        return userService.checkUsername(username);
    }

    @GetMapping("/exists/nickname/{nickname}")
    public Boolean checkNickname(@PathVariable String nickname) {
        return userService.checkNickname(nickname);
    }

    @GetMapping("/exists/email/{email}")
    public Boolean checkEmail(@PathVariable String email) {
        return userService.checkEmail(email);
    }
}
