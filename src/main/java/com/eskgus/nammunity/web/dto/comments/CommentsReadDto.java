package com.eskgus.nammunity.web.dto.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class CommentsReadDto {
    private String content;
    private String createdDate;
    private String modifiedDate;
    private String author;

    public CommentsReadDto(Comments comments) {
        this.content = comments.getContent();
        this.createdDate = comments.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
        this.modifiedDate = comments.getModifiedDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
        this.author = comments.getUser().getNickname();
    }
}
