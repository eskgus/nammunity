package com.eskgus.nammunity.web.dto.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.reports.ContentReportSummary;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.util.DateTimeUtil;
import lombok.Builder;
import lombok.Getter;

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

    private Boolean isPostReportSummary = false;  // 게시글/댓글/사용자 존재 여부 (mustache에서 사용)
    private Boolean isCommentReportSummary = false;
    private Boolean isUserReportSummary = false;

    @Builder
    public ContentReportSummaryDto(ContentReportSummary reportSummary) {
        this.type = reportSummary.getTypes().getDetail();
        this.reporter = reportSummary.getReporter().getNickname();
        this.reportedDate = DateTimeUtil.formatDateTime(reportSummary.getReportedDate());

        String reasonDetail = reportSummary.getReasons().getDetail();
        this.reason = reasonDetail.equals("기타") ? reasonDetail + ": " + reportSummary.getOtherReasons() : reasonDetail;

        if (this.type.equals(ContentType.POSTS.getDetail())) {
            this.isPostReportSummary = true;
            this.post = reportSummary.getPosts();
            this.postId = this.post.getId();
            this.title = this.post.getTitle();
            this.author = this.post.getUser().getNickname();
            this.modifiedDate = DateTimeUtil.formatDateTime(this.post.getModifiedDate());
        } else if (this.type.equals(ContentType.COMMENTS.getDetail())) {
            this.isCommentReportSummary = true;
            this.comment = reportSummary.getComments();
            this.commentId = this.comment.getId();
            this.content = this.comment.getContent();
            this.author = this.comment.getUser().getNickname();
            this.modifiedDate = DateTimeUtil.formatDateTime(this.comment.getModifiedDate());

            this.post = this.comment.getPosts();
            this.postId = this.post.getId();
            this.title = this.post.getTitle();
        } else {
            this.isUserReportSummary = true;
            this.user = reportSummary.getUser();
            this.userId = this.user.getId();
            this.nickname = this.user.getNickname();
            this.createdDate = DateTimeUtil.formatDateTime(this.user.getCreatedDate());
        }
    }
}
