package com.eskgus.nammunity.web.dto.pagination;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ContentsPageMoreDtos<T, U, V> {
    private final ContentsPageMoreDto<T> contentsPageMore1;
    private final ContentsPageMoreDto<U> contentsPageMore2;
    private final ContentsPageMoreDto<V> contentsPageMore3;

    @Builder
    public ContentsPageMoreDtos(ContentsPageMoreDto<T> contentsPageMore1,
                                ContentsPageMoreDto<U> contentsPageMore2,
                                ContentsPageMoreDto<V> contentsPageMore3) {
        this.contentsPageMore1 = contentsPageMore1;
        this.contentsPageMore2 = contentsPageMore2;
        this.contentsPageMore3 = contentsPageMore3;
    }
}
