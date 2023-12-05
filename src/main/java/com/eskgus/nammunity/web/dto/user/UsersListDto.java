package com.eskgus.nammunity.web.dto.user;

import com.eskgus.nammunity.domain.user.User;
import lombok.Getter;

@Getter
public class UsersListDto {
    private Long id;
    private String nickname;

    public UsersListDto(User user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
    }
}
