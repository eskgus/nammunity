package com.eskgus.nammunity.web.dto.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.user.User;
import lombok.Getter;

import static com.eskgus.nammunity.util.DateTimeUtil.formatDateTime;
import static com.eskgus.nammunity.util.DateTimeUtil.formatModifiedDate;

@Getter
public class CommentsReadDto {
    private Long id;
    private String author;
    private Long authorId;
    private String content;
    private String createdDate;
    private String modifiedDate;

    private boolean doesUserWriteComment;

    private Long likes;
    private boolean doesUserLikeComment;

    public CommentsReadDto(Comments comment, Long likes) {
        this.id = comment.getId();
        generateAuthor(comment.getUser());
        this.content = comment.getContent();
        this.createdDate = formatDateTime(comment.getCreatedDate());
        this.modifiedDate = formatModifiedDate(comment.getCreatedDate(), comment.getModifiedDate());
        this.likes = likes;
    }

    private void generateAuthor(User user) {
        this.author = user.getNickname();
        this.authorId = user.getId();
    }

    public void setDoesUserWriteComment(boolean doesUserWriteComment) {
        this.doesUserWriteComment = doesUserWriteComment;
    }

    public void setDoesUserLikeComment(boolean doesUserLikeComment) {
        this.doesUserLikeComment = doesUserLikeComment;
    }
}
