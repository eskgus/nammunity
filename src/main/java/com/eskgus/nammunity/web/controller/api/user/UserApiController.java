package com.eskgus.nammunity.web.controller.api.user;

import com.eskgus.nammunity.service.user.RegistrationService;
import com.eskgus.nammunity.service.user.UserUpdateService;
import com.eskgus.nammunity.web.dto.user.EmailUpdateDto;
import com.eskgus.nammunity.web.dto.user.NicknameUpdateDto;
import com.eskgus.nammunity.web.dto.user.PasswordUpdateDto;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserApiController {
    private final RegistrationService registrationService;
    private final UserUpdateService userUpdateService;

    @PostMapping
    public ResponseEntity<Long> signUp(@Valid @RequestBody RegistrationDto registrationDto) {
        Long id = registrationService.signUp(registrationDto);
        return ResponseEntity.status(HttpStatus.OK).body(id);
    }

    @GetMapping("/validation")
    public ResponseEntity<Void> check(@RequestParam(name = "username", required = false) String username,
                                      @RequestParam(name = "nickname", required = false) String nickname,
                                      @RequestParam(name = "email", required = false) String email) {
        registrationService.check(username, nickname, email);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody PasswordUpdateDto requestDto,
                                               Principal principal) {
        userUpdateService.updatePassword(requestDto, principal);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/nickname")
    public ResponseEntity<Void> updateNickname(@Valid @RequestBody NicknameUpdateDto requestDto,
                                               Principal principal) {
        userUpdateService.updateNickname(requestDto, principal);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/email")
    public ResponseEntity<Void> updateEmail(@Valid @RequestBody EmailUpdateDto requestDto,
                                            Principal principal) {
        userUpdateService.updateEmail(requestDto, principal);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(Principal principal,
                                           @CookieValue(name = "access_token", required = false) String accessToken) {
        HttpHeaders headers = userUpdateService.deleteUser(principal, accessToken);
        return ResponseEntity.status(HttpStatus.OK).headers(headers).build();
    }

    @PostMapping("/unlink/{social}")
    public ResponseEntity<Void> unlinkSocial(@PathVariable String social,
                                             @CookieValue(name = "access_token", required = false) String accessToken,
                                             Principal principal) {
        HttpHeaders headers = userUpdateService.unlinkSocial(principal, social, accessToken);
        return ResponseEntity.status(HttpStatus.OK).headers(headers).build();
    }
}
