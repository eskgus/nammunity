package com.eskgus.nammunity.web.dto.reports;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ContentReportDetailRequestDto {
    private final Long postId;
    private final Long commentId;
    private final Long userId;
    private final int page;

    @Builder
    public ContentReportDetailRequestDto(Long postId, Long commentId, Long userId, int page) {
        this.postId = postId;
        this.commentId = commentId;
        this.userId = userId;
        this.page = page;
    }
}
