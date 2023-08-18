package com.eskgus.nammunity.web.dto.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.util.DateTimeUtil;
import lombok.Getter;

@Getter
public class CommentsReadDto {
    private Long id;
    private String content;
    private String createdDate;
    private String modifiedDate;
    private String author;
    private Boolean cAuth = null;
    private int lSum;
    private Boolean lAuth = null;

    public CommentsReadDto(Comments comments, User user) {
        this.id = comments.getId();
        this.content = comments.getContent();
        this.createdDate = DateTimeUtil.formatDateTime(comments.getCreatedDate());
        this.modifiedDate = DateTimeUtil.formatModifiedDate(comments.getCreatedDate(), comments.getModifiedDate());
        this.author = comments.getUser().getNickname();
        this.lSum = comments.getLikes().size();
        comments.getLikes().forEach(like -> {
            if (like.getUser().equals(user)) {
                this.lAuth = true;
            }
        });

        if (comments.getUser().equals(user)) {
            this.cAuth = true;
        }
    }
}
