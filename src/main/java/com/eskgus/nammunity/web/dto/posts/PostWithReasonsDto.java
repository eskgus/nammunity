package com.eskgus.nammunity.web.dto.posts;

import com.eskgus.nammunity.web.dto.reports.ReasonsListDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class PostWithReasonsDto {
    private PostsReadDto post;
    private List<ReasonsListDto> reasons;

    @Builder
    public PostWithReasonsDto(PostsReadDto post, List<ReasonsListDto> reasons) {
        this.post = post;
        this.reasons = reasons;
    }
}
