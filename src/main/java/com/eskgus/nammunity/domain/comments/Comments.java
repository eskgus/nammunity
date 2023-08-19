package com.eskgus.nammunity.domain.comments;

import com.eskgus.nammunity.domain.BaseTimeEntity;
import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.reports.CommunityReports;
import com.eskgus.nammunity.domain.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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

    @OneToMany(mappedBy = "comments", cascade = CascadeType.REMOVE)
    private List<Likes> likes;

    @OneToMany(mappedBy = "comments", cascade = CascadeType.REMOVE)
    private List<CommunityReports> reports;

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
