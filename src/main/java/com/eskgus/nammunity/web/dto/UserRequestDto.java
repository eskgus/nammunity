package com.eskgus.nammunity.web.dto;

import com.eskgus.nammunity.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserRequestDto {
    private String username;
    private String password;
    private String nickname;

    @Builder
    public UserRequestDto(String username, String password, String nickname) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
    }

    public User toEntity() {
        return User.builder().username(username).password(password).nickname(nickname).build();
    }
}
