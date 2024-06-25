package com.eskgus.nammunity.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentType {
    POSTS("posts", "게시글"),
    COMMENTS("comments", "댓글"),
    USERS("users", "사용자");

    private final String name;
    private final String detail;
}
