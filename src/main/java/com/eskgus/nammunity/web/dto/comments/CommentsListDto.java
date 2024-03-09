package com.eskgus.nammunity.web.dto.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.util.DateTimeUtil;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CommentsListDto {
    private Long commentsId;
    private String author;
    private String content;
    private String modifiedDate;
    private Long postsId;
    private String title;
    private Long likes;

    @Builder
    public CommentsListDto(Comments comment, Long likes) {
        generateComments(comment);
        this.likes = likes;
    }

    public CommentsListDto(Comments comment) {
        generateComments(comment);
    }

    private void generateComments(Comments comment) {
        this.commentsId = comment.getId();
        this.author = comment.getUser().getNickname();
        this.content = comment.getContent();
        this.modifiedDate = DateTimeUtil.formatDateTime(comment.getModifiedDate());
        this.postsId = comment.getPosts().getId();
        this.title = comment.getPosts().getTitle();
    }
}
