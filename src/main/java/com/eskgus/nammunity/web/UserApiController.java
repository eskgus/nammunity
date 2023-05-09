package com.eskgus.nammunity.web;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.tokens.TokensService;
import com.eskgus.nammunity.service.user.RegistrationService;
import com.eskgus.nammunity.service.user.SignInService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserApiController {
    private final UserService userService;
    private final RegistrationService registrationService;
    private final TokensService tokensService;
    private final SignInService signInService;

    @PostMapping
    public Long signUp(@Valid @RequestBody RegistrationDto registrationDto) {
        registrationService.register(registrationDto);
        return userService.findByUsername(registrationDto.getUsername()).getId();
    }

    @GetMapping("/confirm")
    public RedirectView confirmToken(@RequestParam String token, RedirectAttributes ra) {
        try {
            registrationService.confirmToken(token);
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
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
        User user = userService.findById(id);
        if (LocalDateTime.now().isAfter(user.getCreatedDate().plusMinutes(12))) {
            tokensService.deleteAllByUser(user);
            userService.delete(user);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "resendToken");
        }

        registrationService.sendToken(id);
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

    @GetMapping("/find/username/{email}")
    public String findUsername(@PathVariable String email) {
        String username;
        try {
            username = signInService.findUsername(email);
        } catch (IllegalArgumentException ex) {
            return ex.getMessage();
        }
        return "가입하신 ID는 " + username.substring(0, 3) + "****입니다.";
    }

    @PutMapping("/find/password/{username}")
    public String findPassword(@PathVariable String username) {
        try {
            signInService.findPassword(username);
        } catch (IllegalArgumentException ex) {
            return ex.getMessage();
        }
        return "가입된 이메일로 임시 비밀번호를 보냈습니다.";
    }
}
