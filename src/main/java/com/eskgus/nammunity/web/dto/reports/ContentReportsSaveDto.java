package com.eskgus.nammunity.web.dto.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.reports.ContentReports;
import com.eskgus.nammunity.domain.reports.Reasons;
import com.eskgus.nammunity.domain.reports.Types;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.validation.CustomNotNull;
import com.eskgus.nammunity.validation.CustomSize;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.EMPTY_REASON;
import static com.eskgus.nammunity.domain.enums.ExceptionMessages.INVALID_OTHER_REASON;

@Setter
@Getter
@NoArgsConstructor
public class ContentReportsSaveDto {
    @CustomNotNull(exceptionMessage = EMPTY_REASON)
    private Long reasonsId;

    private Long postsId;
    private Long commentsId;
    private Long userId;

    private Posts posts;
    private Comments comments;
    private User user;
    private User reporter;
    private Types types;
    private Reasons reasons;

    @CustomSize(exceptionMessage = INVALID_OTHER_REASON, max = 500)
    private String otherReasons;

    @Builder
    public ContentReportsSaveDto(Posts posts, Comments comments, User user,
                                 User reporter, Types types, Reasons reasons, String otherReasons) {
        this.posts = posts;
        this.comments = comments;
        this.user = user;
        this.reporter = reporter;
        this.types = types;
        this.reasons = reasons;
        this.otherReasons = otherReasons;
    }

    public ContentReports toEntity() {
        return ContentReports.builder()
                .posts(posts).comments(comments).user(user)
                .reporter(reporter).types(types)
                .reasons(reasons).otherReasons(otherReasons).build();
    }
}
