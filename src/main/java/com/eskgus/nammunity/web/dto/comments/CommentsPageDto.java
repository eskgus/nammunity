package com.eskgus.nammunity.web.dto.comments;

import com.eskgus.nammunity.web.dto.pagination.PaginationDto;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class CommentsPageDto {
    private Page<CommentsReadDto> comments;
    private PaginationDto<CommentsReadDto> pages;

    @Builder
    public CommentsPageDto(Page<CommentsReadDto> comments, PaginationDto<CommentsReadDto> pages) {
        this.comments = comments;
        this.pages = pages;
    }
}
