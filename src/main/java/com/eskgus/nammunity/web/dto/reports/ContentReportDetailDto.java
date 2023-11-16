package com.eskgus.nammunity.web.dto.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.util.DateTimeUtil;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ContentReportDetailDto {
    private Posts post;
    private Comments comment;
    private User user;

    private String title;
    private String content;
    private String author;
    private String modifiedDate;    // 작성일(수정일)
    private String nickname;        // 신고된 사용자
    private String createdDate;     // 가입일

    private String type;

    private Boolean postExistence;  // 게시글/댓글/사용자 존재 여부 (mustache에서 사용)
    private Boolean commentExistence;
    private Boolean userExistence;

    private List<ContentReportDetailListDto> reports;
    private int numOfReports;   // 신고 세부 내역 개수

    @Builder
    public ContentReportDetailDto(Posts post, Comments comment, User user,
                                  List<ContentReportDetailListDto> detailListDtos, int numOfReports) {
        if (post != null) {
            this.post = post;
            this.title = this.post.getTitle();
            this.author = this.post.getUser().getNickname();
            this.modifiedDate = DateTimeUtil.formatDateTime(this.post.getModifiedDate());
            this.type = "게시글";
            this.postExistence = true;
        } else if (comment != null) {
            this.comment = comment;
            this.content = this.comment.getContent();
            this.author = this.comment.getUser().getNickname();
            this.modifiedDate = DateTimeUtil.formatDateTime(this.comment.getModifiedDate());
            this.type = "댓글";
            this.commentExistence = true;
        } else {
            this.user = user;
            this.nickname = this.user.getNickname();
            this.createdDate = DateTimeUtil.formatDateTime(this.user.getCreatedDate());
            this.type = "사용자";
            this.userExistence = true;
        }

        this.reports = detailListDtos;
        this.numOfReports = numOfReports;
    }
}
