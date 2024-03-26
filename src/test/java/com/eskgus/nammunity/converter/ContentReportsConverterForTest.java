package com.eskgus.nammunity.converter;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.reports.ContentReports;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.reports.ContentReportDetailListDto;

public class ContentReportsConverterForTest implements EntityConverterForTest<ContentReports, ContentReportDetailListDto> {
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
    public Long extractListDtoId(ContentReportDetailListDto listDto) {
        return listDto.getId();
    }

    @Override
    public ContentReportDetailListDto generateListDto(ContentReports entity) {
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
