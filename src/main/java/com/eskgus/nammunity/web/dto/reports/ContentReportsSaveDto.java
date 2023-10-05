package com.eskgus.nammunity.web.dto.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.reports.ContentReports;
import com.eskgus.nammunity.domain.reports.Reasons;
import com.eskgus.nammunity.domain.reports.Types;
import com.eskgus.nammunity.domain.user.User;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ContentReportsSaveDto {
    @NotNull(message = "신고 사유를 선택하세요.")
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

    @Size(max = 500, message = "기타 사유는 500글자 이하로 작성해 주세요.")
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
