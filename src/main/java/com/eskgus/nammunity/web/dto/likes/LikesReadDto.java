package com.eskgus.nammunity.web.dto.likes;

import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.user.User;
import lombok.Getter;

@Getter
public class LikesReadDto {
    private Long id;
    private User user;

    public LikesReadDto(Likes likes) {
        this.id = likes.getId();
        this.user = likes.getUser();
    }
}
