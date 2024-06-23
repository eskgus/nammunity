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
    OTHER_REASONS("otherReasons");

    private final String key;
}
