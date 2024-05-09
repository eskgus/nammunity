package com.eskgus.nammunity.converter;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.reports.ContentReports;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.reports.ContentReportDetailListDto;

public class ContentReportsConverterForTest implements EntityConverterForTest<ContentReportDetailListDto, ContentReports> {
    @Override
    public Long extractEntityId(ContentReports entity) {
        return entity.getId();
    }

    @Override
    public Long extractUserId(ContentReports entity) {
        User user = entity.getUser();
        return user != null ? user.getId() : 0;
    }

    @Override
    public Long extractDtoId(ContentReportDetailListDto dto) {
        return dto.getId();
    }

    @Override
    public ContentReportDetailListDto generateDto(ContentReports entity) {
        return new ContentReportDetailListDto(entity);
    }

    public Long extractPostId(ContentReports entity) {
        Posts post = entity.getPosts();
        return post != null ? post.getId() : 0;
    }

    public Long extractCommentId(ContentReports entity) {
        Comments comment = entity.getComments();
        return comment != null ? comment.getId() : 0;
    }
}
