package com.eskgus.nammunity.web.dto.reports;

import com.eskgus.nammunity.domain.reports.Types;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.user.UsersListDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ContentReportDetailDto <U> {
    private PostsListDto post;
    private CommentsListDto comment;
    private UsersListDto user;

    private String type;

    private Boolean postExistence = false;  // 게시글/댓글/사용자 존재 여부 (mustache에서 사용)
    private Boolean commentExistence = false;
    private Boolean userExistence = false;

    private List<ContentReportDetailListDto> reports;
    private int numOfReports;   // 신고 세부 내역 개수

    @Builder
    public ContentReportDetailDto(Types type, U dto,
                                  List<ContentReportDetailListDto> reports) {
        this.type = type.getDetail();

        if (this.type.equals("게시글")) {
            this.post = (PostsListDto) dto;
            this.postExistence = true;
        } else if (this.type.equals("댓글")) {
            this.comment = (CommentsListDto) dto;
            this.commentExistence = true;
        } else {
            this.user = (UsersListDto) dto;
            this.userExistence = true;
        }

        this.reports = reports;
        this.numOfReports = this.reports.size();
    }
}
