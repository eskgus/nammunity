package com.eskgus.nammunity.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Fields {
    TITLE("title"),
    CONTENT("content");

    private final String key;
}
