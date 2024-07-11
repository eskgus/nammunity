package com.eskgus.nammunity.domain.likes;

import com.eskgus.nammunity.domain.common.BaseTimeEntity;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class Likes extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "Posts_id")
    private Posts posts;

    @ManyToOne
    @JoinColumn(name = "Comments_id")
    private Comments comments;

    @ManyToOne
    @JoinColumn(nullable = false, name = "Users_id")
    private User user;

    @Builder
    public Likes(Posts posts, Comments comments, User user) {
        this.posts = posts;
        this.comments = comments;
        this.user = user;
    }
}
