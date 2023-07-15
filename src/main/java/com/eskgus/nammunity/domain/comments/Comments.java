package com.eskgus.nammunity.domain.comments;

import com.eskgus.nammunity.domain.BaseTimeEntity;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class Comments extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 1500, nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(nullable = false, name = "Posts_id")
    private Posts posts;

    @ManyToOne
    @JoinColumn(nullable = false, name = "Users_id")
    private User user;

    @Builder
    public Comments(String content, Posts posts, User user) {
        this.content = content;
        this.posts = posts;
        this.user = user;
    }

    public void update(String content) {
        this.content = content;
    }
}
