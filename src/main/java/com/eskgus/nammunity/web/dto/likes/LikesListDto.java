package com.eskgus.nammunity.web.dto.likes;

import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.user.User;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class LikesListDto {
    private Long likesId;
    private User user;
    private String createdDate;
    private Long postsId;
    private String title;
    private Boolean comments = null;
    private Long commentsId;
    private String content;

    public LikesListDto(Likes likes) {
        this.likesId = likes.getId();
        this.user = likes.getUser();
        this.createdDate = likes.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));

        if (likes.getPosts() != null) {
            this.postsId = likes.getPosts().getId();
            this.title = likes.getPosts().getTitle();
        } else {
            this.postsId = likes.getComments().getPosts().getId();
            this.title = likes.getComments().getPosts().getTitle();
            this.comments = true;
            this.commentsId = likes.getComments().getId();
            this.content = likes.getComments().getContent();
        }
    }
}
