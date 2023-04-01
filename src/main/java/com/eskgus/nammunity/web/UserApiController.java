package com.eskgus.nammunity.web;

import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.UserRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class UserApiController {
    private final UserService userService;

    @PostMapping("/api/user")
    public Long signUp(@RequestBody UserRequestDto requestDto) {
        return userService.signUp(requestDto);
    }
}
