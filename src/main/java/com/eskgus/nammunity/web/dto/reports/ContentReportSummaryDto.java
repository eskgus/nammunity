package com.eskgus.nammunity.web.dto.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.util.DateTimeUtil;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ContentReportSummaryDto {
    private Posts post;
    private Comments comment;
    private User user;

    private Long postId;
    private Long commentId;
    private Long userId;

    private String title;
    private String content;
    private String author;
    private String modifiedDate;    // 작성일(수정일)
    private String nickname;        // 신고된 사용자
    private String createdDate;     // 가입일

    private String type;
    private String reporter;
    private String reportedDate;    // 신고일
    private String reason;

    private Boolean postExistence;  // 게시글/댓글/사용자 존재 여부 (mustache에서 사용)
    private Boolean commentExistence;
    private Boolean userExistence;

    @Builder
    public ContentReportSummaryDto(ContentReportDistinctDto distinctDto,
                                   User reporter, LocalDateTime reportedDate, String reason) {
        this.type = distinctDto.getTypes().getDetail();
        this.reporter = reporter.getNickname();
        this.reportedDate = DateTimeUtil.formatDateTime(reportedDate);
        this.reason = reason;

        if (distinctDto.getPosts() != null) {
            this.post = distinctDto.getPosts();
            this.postId = this.post.getId();
            this.title = this.post.getTitle();
            this.author = this.post.getUser().getNickname();
            this.modifiedDate = DateTimeUtil.formatDateTime(this.post.getModifiedDate());
            this.postExistence = true;
        } else if (distinctDto.getComments() != null) {
            this.comment = distinctDto.getComments();
            this.commentId = this.comment.getId();
            this.content = this.comment.getContent();
            this.author = this.comment.getUser().getNickname();
            this.modifiedDate = DateTimeUtil.formatDateTime(this.comment.getModifiedDate());
            this.post = this.comment.getPosts();
            this.postId = this.post.getId();
            this.title = this.post.getTitle();
            this.commentExistence = true;
        } else {
            this.user = distinctDto.getUser();
            this.userId = this.user.getId();
            this.nickname = this.user.getNickname();
            this.createdDate = DateTimeUtil.formatDateTime(this.user.getCreatedDate());
            this.userExistence = true;
        }
    }
}
