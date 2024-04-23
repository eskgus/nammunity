package com.eskgus.nammunity.web.dto.user;

import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PostsHistoryDto {
    private final ContentsPageDto<PostsListDto> contentsPage;
    private final long numberOfPosts;
    private final long numberOfComments;

    @Builder
    public PostsHistoryDto(ContentsPageDto<PostsListDto> contentsPage, long numberOfComments) {
        this.contentsPage = contentsPage;
        this.numberOfPosts = contentsPage.getContents().getTotalElements();
        this.numberOfComments = numberOfComments;
    }
}
