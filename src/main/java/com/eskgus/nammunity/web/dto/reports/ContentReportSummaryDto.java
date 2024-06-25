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

import static com.eskgus.nammunity.domain.enums.ContentType.*;
import static com.eskgus.nammunity.domain.enums.Fields.OTHER;

@Getter
public class ContentReportSummaryDto {
    private Long id;
    private String type;
    private String reporter;
    private String reportedDate;
    private String reason;

    private final PostsListDto postsListDto;
    private final CommentsListDto commentsListDto;
    private final UsersListDto usersListDto;

    @Builder
    public ContentReportSummaryDto(ContentReportSummary reportSummary, Posts post, Comments comment, User user) {
        generateReportSummary(reportSummary);

        this.postsListDto = generateListDtoByType(POSTS, PostsListDto::new, post);
        this.commentsListDto = generateListDtoByType(COMMENTS, CommentsListDto::new, comment);
        this.usersListDto = generateListDtoByType(USERS, UsersListDto::new, user);
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
        if (OTHER.getKey().equals(reasonDetail)) {
            return reasonDetail + ": " + reportSummary.getOtherReasons();
        }
        return reasonDetail;
    }

    private <T, U> T generateListDtoByType(ContentType contentType, Function<U, T> constructor, U entity) {
        if (contentType.getDetail().equals(type)) {
            return constructor.apply(entity);
        }
        return null;
    }
}
