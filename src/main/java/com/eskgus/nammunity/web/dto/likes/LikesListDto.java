package com.eskgus.nammunity.web.dto.likes;

import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.util.DateTimeUtil;
import lombok.Getter;

@Getter
public class LikesListDto {
    private Long likesId;
    private User user;
    private String createdDate;
    private Long postsId;
    private String title;
    private Boolean isCommentLikes = false;
    private Long commentsId;
    private String content;
    private String author;

    public LikesListDto(Likes likes) {
        this.likesId = likes.getId();
        this.user = likes.getUser();
        this.createdDate = DateTimeUtil.formatDateTime(likes.getCreatedDate());

        if (likes.getPosts() != null) {
            this.postsId = likes.getPosts().getId();
            this.title = likes.getPosts().getTitle();
            this.author = likes.getPosts().getUser().getNickname();
        } else {
            this.postsId = likes.getComments().getPosts().getId();
            this.title = likes.getComments().getPosts().getTitle();
            this.isCommentLikes = true;
            this.commentsId = likes.getComments().getId();
            this.content = likes.getComments().getContent();
            this.author = likes.getComments().getUser().getNickname();
        }
    }
}
