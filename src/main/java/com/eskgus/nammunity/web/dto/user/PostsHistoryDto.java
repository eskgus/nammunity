package com.eskgus.nammunity.web.dto.user;

import com.eskgus.nammunity.web.dto.pagination.PaginationDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class PostsHistoryDto {
    private Page<PostsListDto> posts;
    private PaginationDto<PostsListDto> pages;
    private long numberOfPosts;
    private long numberOfComments;

    @Builder
    public PostsHistoryDto(Page<PostsListDto> posts, PaginationDto<PostsListDto> pages,
                           long numberOfComments) {
        this.posts = posts;
        this.pages = pages;
        this.numberOfPosts = posts.getTotalElements();
        this.numberOfComments = numberOfComments;
    }
}
