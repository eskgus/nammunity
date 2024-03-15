package com.eskgus.nammunity.web.dto.reports;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ContentReportSummaryDeleteDto {
    private List<Long> postsId;
    private List<Long> commentsId;
    private List<Long> userId;

    @Builder
    public ContentReportSummaryDeleteDto(List<Long> postsId, List<Long> commentsId, List<Long> userId) {
        this.postsId = postsId;
        this.commentsId = commentsId;
        this.userId = userId;
    }
}
