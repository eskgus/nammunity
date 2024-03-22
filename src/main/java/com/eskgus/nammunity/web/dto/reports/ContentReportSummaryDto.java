package com.eskgus.nammunity.web.dto.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.reports.ContentReportSummary;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.util.DateTimeUtil;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.user.UsersListDto;
import lombok.Builder;
import lombok.Getter;

import java.util.function.Function;

@Getter
public class ContentReportSummaryDto {
    private Long id;
    private String type;
    private String reporter;
    private String reportedDate;
    private String reason;

    private PostsListDto postsListDto;
    private CommentsListDto commentsListDto;
    private UsersListDto usersListDto;

    @Builder
    public ContentReportSummaryDto(ContentReportSummary reportSummary, Posts post, Comments comment, User user) {
        generateReportSummary(reportSummary);

        this.postsListDto = generateListDtoByType(ContentType.POSTS, PostsListDto::new, post);
        this.commentsListDto = generateListDtoByType(ContentType.COMMENTS, CommentsListDto::new, comment);
        this.usersListDto = generateListDtoByType(ContentType.USERS, UsersListDto::new, user);
    }

    private void generateReportSummary(ContentReportSummary reportSummary) {
        this.id = reportSummary.getId();
        this.type = reportSummary.getTypes().getDetail();
        this.reporter = reportSummary.getReporter().getNickname();
        this.reportedDate = DateTimeUtil.formatDateTime(reportSummary.getReportedDate());
        this.reason = generateReason(reportSummary);
    }

    private String generateReason(ContentReportSummary reportSummary) {
        String reasonDetail = reportSummary.getReasons().getDetail();
        if (reasonDetail.equals("기타")) {
            return reasonDetail + ": " + reportSummary.getOtherReasons();
        }
        return reasonDetail;
    }

    private <T, U> T generateListDtoByType(ContentType contentType, Function<U, T> constructor, U entity) {
        if (type.equals(contentType.getDetailInKor())) {
            return constructor.apply(entity);
        }
        return null;
    }
}
