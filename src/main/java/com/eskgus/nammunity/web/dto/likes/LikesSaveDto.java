package com.eskgus.nammunity.web.dto.likes;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LikesSaveDto {
    private Posts posts;
    private Comments comments;
    private User user;

    @Builder
    public LikesSaveDto(Posts posts, Comments comments, User user) {
        this.posts = posts;
        this.comments = comments;
        this.user = user;
    }

    public Likes toEntity() {
        return Likes.builder().posts(posts).comments(comments).user(user).build();
    }
}
