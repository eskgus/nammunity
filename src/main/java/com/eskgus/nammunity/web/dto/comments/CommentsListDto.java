package com.eskgus.nammunity.web.dto.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.util.DateTimeUtil;
import lombok.Getter;

@Getter
public class CommentsListDto {
    private Long commentsId;
    private String content;
    private String modifiedDate;
    private Long postsId;
    private String title;
    private int likes;

    public CommentsListDto(Comments comments) {
        this.commentsId = comments.getId();
        this.content = comments.getContent();
        this.modifiedDate = DateTimeUtil.formatDateTime(comments.getModifiedDate());
        this.postsId = comments.getPosts().getId();
        this.title = comments.getPosts().getTitle();
        this.likes = comments.getLikes().size();
    }
}
