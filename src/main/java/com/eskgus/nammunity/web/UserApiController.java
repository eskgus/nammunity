package com.eskgus.nammunity.web;

import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.UserRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@RestController
public class UserApiController {
    private final UserService userService;

    @PostMapping("/api/user")
    public Long signUp(@Valid @RequestBody UserRequestDto requestDto) {
        if (userService.checkUsername(requestDto.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username");
        } else if (userService.checkNickname(requestDto.getNickname())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "nickname");
        } else if (!requestDto.getPassword().equals(requestDto.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "confirmPassword");
        }
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
