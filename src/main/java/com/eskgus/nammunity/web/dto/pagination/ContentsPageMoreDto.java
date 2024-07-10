package com.eskgus.nammunity.web.dto.pagination;

import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class ContentsPageMoreDto<Dto> {
    private final Page<Dto> contents;
    private boolean more;

    public ContentsPageMoreDto(Page<Dto> contents) {
        this.contents = contents;
        generateMore();
    }

    private void generateMore() {
        this.more = contents.getTotalElements() > 5;
    }
}
