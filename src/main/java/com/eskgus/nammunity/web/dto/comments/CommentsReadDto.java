package com.eskgus.nammunity.web.dto.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.user.User;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class CommentsReadDto {
    private Long id;
    private String content;
    private String createdDate;
    private String modifiedDate = null;
    private String author;
    private Boolean auth = null;

    public CommentsReadDto(Comments comments, User user) {
        this.id = comments.getId();
        this.content = comments.getContent();
        this.createdDate = comments.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
        this.author = comments.getUser().getNickname();

        if (!comments.getCreatedDate().equals(comments.getModifiedDate())) {
            this.modifiedDate = comments.getModifiedDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
        }

        if (comments.getUser().equals(user)) {
            this.auth = true;
        }
    }
}
