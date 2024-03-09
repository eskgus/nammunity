package com.eskgus.nammunity.web.dto.user;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.util.DateTimeUtil;
import lombok.Getter;

@Getter
public class UsersListDto {
    private Long id;
    private String nickname;
    private String createdDate;

    public UsersListDto(User user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.createdDate = DateTimeUtil.formatDateTime(user.getCreatedDate());
    }
}
