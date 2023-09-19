package com.eskgus.nammunity.web.dto.reports;

import lombok.Getter;

import java.util.List;

@Getter
public class ContentReportsDeleteDto {
    private List<Long> postsId;
    private List<Long> commentsId;
    private List<Long> userId;
}
