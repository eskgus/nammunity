package com.eskgus.nammunity.domain.posts;

import com.eskgus.nammunity.domain.common.BaseTimeEntity;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.common.Element;
import com.eskgus.nammunity.domain.common.Visitor;
import com.eskgus.nammunity.domain.likes.Likes;
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
public class Posts extends BaseTimeEntity implements Element {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 3000, nullable = false)
    private String content;

    @Column(columnDefinition = "int default 0", nullable = false)
    private int view;

    @ManyToOne
    @JoinColumn(nullable = false, name = "Users_id")
    private User user;

    @OneToMany(mappedBy = "posts", cascade = CascadeType.REMOVE)
    private List<Comments> comments;

    @OneToMany(mappedBy = "posts", cascade = CascadeType.REMOVE)
    private List<Likes> likes;

    @OneToMany(mappedBy = "posts", cascade = CascadeType.REMOVE)
    private List<ContentReports> receivedReports;

    @OneToOne(mappedBy = "posts", cascade = CascadeType.REMOVE)
    private ContentReportSummary receivedReportSummary;

    @Builder
    public Posts(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.user = user;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void countView() {
        this.view += 1;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
