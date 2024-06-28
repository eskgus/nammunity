package com.eskgus.nammunity.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Fields {
    TITLE("title"),
    CONTENT("content"),
    POSTS_ID("postsId"),
    COMMENTS_ID("commentsId"),
    USER_ID("userId"),
    REASONS_ID("reasonsId"),
    OTHER_REASONS("otherReasons"),
    USERNAME("username"),
    EMAIL("email"),
    OLD_PASSWORD("oldPassword"),
    PASSWORD("password"),
    CONFIRM_PASSWORD("confirmPassword"),
    NICKNAME("nickname"),
    SOCIAL("social"),
    NAME("name"),
    ACCESS_TOKEN("accessToken"),
    REFRESH_TOKEN("refreshToken"),
    TOKEN("token"),
    OTHER("기타");

    private final String key;
}
