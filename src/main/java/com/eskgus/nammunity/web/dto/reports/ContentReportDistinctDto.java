package com.eskgus.nammunity.web.dto.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.reports.Types;
import com.eskgus.nammunity.domain.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ContentReportDistinctDto {
    private Types types;
    private Posts posts;
    private Comments comments;
    private User user;

    @Builder
    public ContentReportDistinctDto(Types types, Posts posts, Comments comments, User user) {
        this.types = types;
        this.posts = posts;
        this.comments = comments;
        this.user = user;
    }
}
