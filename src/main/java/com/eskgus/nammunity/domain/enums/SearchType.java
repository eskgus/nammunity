package com.eskgus.nammunity.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SearchType {
    TITLE("title"),
    CONTENT("content"),
    TITLE_AND_CONTENT("title content");

    private final String key;

    public static SearchType convertSearchBy(String searchBy) {
        for (SearchType searchType : SearchType.values()) {
            if (searchBy.equals(searchType.getKey())) {
                return searchType;
            }
        }
        return TITLE_AND_CONTENT;
    }
}
