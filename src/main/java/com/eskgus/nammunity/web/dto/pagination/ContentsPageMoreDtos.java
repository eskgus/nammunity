package com.eskgus.nammunity.web.dto.pagination;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ContentsPageMoreDtos<DtoA, DtoB, DtoC> {
    private final ContentsPageMoreDto<DtoA> contentsPageMore1;
    private final ContentsPageMoreDto<DtoB> contentsPageMore2;
    private final ContentsPageMoreDto<DtoC> contentsPageMore3;

    @Builder
    public ContentsPageMoreDtos(ContentsPageMoreDto<DtoA> contentsPageMore1,
                                ContentsPageMoreDto<DtoB> contentsPageMore2,
                                ContentsPageMoreDto<DtoC> contentsPageMore3) {
        this.contentsPageMore1 = contentsPageMore1;
        this.contentsPageMore2 = contentsPageMore2;
        this.contentsPageMore3 = contentsPageMore3;
    }
}
