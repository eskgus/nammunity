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
    private Long authorId;
    private Boolean cAuth;   // 댓글 작성자 확인
    private int lSum;   // 좋아요 개수
    private Boolean lAuth;   // 좋아요 누른 사용자 확인

    public CommentsReadDto(Comments comments, User user) {
        this.id = comments.getId();
        this.content = comments.getContent();
        this.createdDate = DateTimeUtil.formatDateTime(comments.getCreatedDate());
        this.modifiedDate = DateTimeUtil.formatModifiedDate(comments.getCreatedDate(), comments.getModifiedDate());
        this.author = comments.getUser().getNickname();
        this.authorId = comments.getUser().getId();
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
