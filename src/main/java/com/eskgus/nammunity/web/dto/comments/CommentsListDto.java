package com.eskgus.nammunity.web.dto.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class CommentsListDto {
    private Long commentsId;
    private String content;
    private String modifiedDate;
    private Long postsId;
    private String title;

    public CommentsListDto(Comments comments) {
        this.commentsId = comments.getId();
        this.content = comments.getContent();
        this.modifiedDate = comments.getModifiedDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
        this.postsId = comments.getPosts().getId();
        this.title = comments.getPosts().getTitle();
    }
}
