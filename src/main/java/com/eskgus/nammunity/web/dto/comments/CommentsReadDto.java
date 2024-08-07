package com.eskgus.nammunity.web.dto.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.util.DateTimeUtil;
import lombok.Getter;

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
        generateComment(comment);
        this.likes = likes;
    }

    public CommentsReadDto(Comments comment) {
        generateComment(comment);
    }

    private void generateComment(Comments comment) {
        this.id = comment.getId();
        generateAuthor(comment.getUser());
        this.content = comment.getContent();
        this.createdDate = DateTimeUtil.formatDateTime(comment.getCreatedDate());
        this.modifiedDate = DateTimeUtil.formatModifiedDate(comment.getCreatedDate(), comment.getModifiedDate());
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
