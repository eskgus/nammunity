package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.BaseTimeEntity;
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
public class ContentReports extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private Posts posts;

    @ManyToOne
    private Comments comments;

    @ManyToOne
    @JoinColumn(name = "users_id")
    private User user;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User reporter;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Types types;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Reasons reasons;

    @Column(length = 500)
    private String otherReasons;

    @Builder
    public ContentReports(Posts posts, Comments comments, User user,
                          User reporter, Types types, Reasons reasons, String otherReasons) {
        this.posts = posts;
        this.comments = comments;
        this.user = user;
        this.reporter = reporter;
        this.types = types;
        this.reasons = reasons;
        this.otherReasons = otherReasons;
    }
}
