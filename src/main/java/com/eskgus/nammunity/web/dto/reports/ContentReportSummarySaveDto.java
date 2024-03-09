package com.eskgus.nammunity.web.dto.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.reports.ContentReportSummary;
import com.eskgus.nammunity.domain.reports.Reasons;
import com.eskgus.nammunity.domain.reports.Types;
import com.eskgus.nammunity.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ContentReportSummarySaveDto {
    private Posts posts;
    private Comments comments;
    private User user;

    private Types types;
    private LocalDateTime reportedDate;
    private User reporter;
    private Reasons reasons;
    private String otherReasons;

    @Builder
    public ContentReportSummarySaveDto(Posts posts, Comments comments, User user,
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

    public ContentReportSummary toEntity() {
        return ContentReportSummary.builder()
                .posts(posts).comments(comments).user(user)
                .types(types).reportedDate(reportedDate).reporter(reporter)
                .reasons(reasons).otherReasons(otherReasons).build();
    }
}
