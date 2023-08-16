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
    private Boolean cAuth = null;
    private int lSum;
    private Boolean lAuth = null;

    public CommentsReadDto(Comments comments, User user) {
        this.id = comments.getId();
        this.content = comments.getContent();
        this.createdDate = comments.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
        this.author = comments.getUser().getNickname();
        this.lSum = comments.getLikes().size();
        comments.getLikes().forEach(like -> {
            if (like.getUser().equals(user)) {
                this.lAuth = true;
            }
        });

        if (!comments.getCreatedDate().equals(comments.getModifiedDate())) {
            this.modifiedDate = comments.getModifiedDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
        }

        if (comments.getUser().equals(user)) {
            this.cAuth = true;
        }
    }
}
