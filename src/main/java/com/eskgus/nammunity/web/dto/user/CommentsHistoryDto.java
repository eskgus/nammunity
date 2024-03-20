package com.eskgus.nammunity.web.dto.user;

import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.pagination.PaginationDto;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class CommentsHistoryDto {
    private Page<CommentsListDto> comments;
    private PaginationDto<CommentsListDto> pages;
    private long numberOfComments;
    private long numberOfPosts;

    @Builder
    public CommentsHistoryDto(Page<CommentsListDto> comments, PaginationDto<CommentsListDto> pages,
                              long numberOfPosts) {
        this.comments = comments;
        this.pages = pages;
        this.numberOfComments = comments.getTotalElements();
        this.numberOfPosts = numberOfPosts;
    }
}
