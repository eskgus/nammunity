package com.eskgus.nammunity.web.dto.reports;

import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.reports.Types;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.user.UsersListDto;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ContentReportDetailDto <U> {
    private final String type;
    private PostsListDto postsListDto;
    private CommentsListDto commentsListDto;
    private UsersListDto usersListDto;
    private final ContentsPageDto<ContentReportDetailListDto> contentsPage;

    @Builder
    public ContentReportDetailDto(Types type, U contentListDto,
                                  ContentsPageDto<ContentReportDetailListDto> contentsPage) {
        this.type = type.getDetail();
        generateContentListDto(contentListDto);
        this.contentsPage = contentsPage;
    }

    private void generateContentListDto(U contentListDto) {
        if (type.equals(ContentType.POSTS.getDetailInKor())) {
            this.postsListDto = (PostsListDto) contentListDto;
        } else if (type.equals(ContentType.COMMENTS.getDetailInKor())) {
            this.commentsListDto = (CommentsListDto) contentListDto;
        } else {
            this.usersListDto = (UsersListDto) contentListDto;
        }
    }
}
