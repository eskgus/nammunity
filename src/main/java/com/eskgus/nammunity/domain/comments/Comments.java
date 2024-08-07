package com.eskgus.nammunity.domain.comments;

import com.eskgus.nammunity.domain.common.BaseTimeEntity;
import com.eskgus.nammunity.domain.common.Element;
import com.eskgus.nammunity.domain.common.Visitor;
import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.reports.ContentReportSummary;
import com.eskgus.nammunity.domain.reports.ContentReports;
import com.eskgus.nammunity.domain.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Entity
public class Comments extends BaseTimeEntity implements Element {
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
    private List<ContentReports> receivedReports;

    @OneToOne(mappedBy = "comments", cascade = CascadeType.REMOVE)
    private ContentReportSummary receivedReportSummary;

    @Builder
    public Comments(String content, Posts posts, User user) {
        this.content = content;
        this.posts = posts;
        this.user = user;
    }

    public void update(String content) {
        this.content = content;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
