package com.eskgus.nammunity.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentType {
    POSTS("게시글"),
    COMMENTS("댓글"),
    USERS("사용자");

    private final String detail;
}
