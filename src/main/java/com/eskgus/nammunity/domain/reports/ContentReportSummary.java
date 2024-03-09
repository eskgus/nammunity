package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
public class ContentReportSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    private Posts posts;

    @OneToOne
    private Comments comments;

    // 신고된 사용자
    @OneToOne
    @JoinColumn(name = "users_id")
    private User user;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Types types;

    private LocalDateTime reportedDate;

    // 신고한 사용자
    @ManyToOne
    @JoinColumn(nullable = false)
    private User reporter;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Reasons reasons;

    @Column(length = 500)
    private String otherReasons;

    @Builder
    public ContentReportSummary(Posts posts, Comments comments, User user,
                                Types types, LocalDateTime reportedDate, User reporter,
                                Reasons reasons, String otherReasons) {
        this.posts = posts;
        this.comments = comments;
        this.user = user;
        this.types = types;
        this.reportedDate = reportedDate;
        this.reporter = reporter;
        this.reasons = reasons;
        this.otherReasons = otherReasons;
    }

    public void update(LocalDateTime reportedDate, User reporter, Reasons reasons, String otherReasons) {
        this.reportedDate = reportedDate;
        this.reporter = reporter;
        this.reasons = reasons;
        this.otherReasons = otherReasons;
    }
}
