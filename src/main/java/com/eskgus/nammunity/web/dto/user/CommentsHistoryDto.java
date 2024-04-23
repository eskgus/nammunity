package com.eskgus.nammunity.web.dto.user;

import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CommentsHistoryDto {
    private final ContentsPageDto<CommentsListDto> contentsPage;
    private final long numberOfComments;
    private final long numberOfPosts;

    @Builder
    public CommentsHistoryDto(ContentsPageDto<CommentsListDto> contentsPage, long numberOfPosts) {
        this.contentsPage = contentsPage;
        this.numberOfComments = contentsPage.getContents().getTotalElements();
        this.numberOfPosts = numberOfPosts;
    }
}
