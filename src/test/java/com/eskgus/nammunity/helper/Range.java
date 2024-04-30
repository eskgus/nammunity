package com.eskgus.nammunity.helper;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Range {
    private final long startIndex;
    private final long endIndex;
    private final String nickname;
    private final String title;
    private final String content;
    private final String comment;

    @Builder
    public Range(long startIndex, long endIndex, String nickname, String title, String content, String comment) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.nickname = nickname;
        this.title = title;
        this.content = content;
        this.comment = comment;
    }
}
