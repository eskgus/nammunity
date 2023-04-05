package com.eskgus.nammunity.web;

import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.UserRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class UserApiController {
    private final UserService userService;

    @PostMapping("/api/user")
    public Long signUp(@Valid @RequestBody UserRequestDto requestDto) {
        return userService.signUp(requestDto);
    }

    @GetMapping("/api/exists/username/{username}")
    public Boolean checkUsername(@PathVariable String username) {
        return userService.checkUsername(username);
    }

    @GetMapping("/api/exists/nickname/{nickname}")
    public Boolean checkNickname(@PathVariable String nickname) {
        return userService.checkNickname(nickname);
    }
}
