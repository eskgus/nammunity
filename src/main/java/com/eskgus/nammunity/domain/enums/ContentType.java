package com.eskgus.nammunity.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentType {
    POSTS("게시글", "posts"),
    COMMENTS("댓글", "comments"),
    USERS("사용자", "users");

    private final String detailInKor;
    private final String detailInEng;
}
