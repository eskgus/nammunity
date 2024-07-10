package com.eskgus.nammunity.web.dto.reports;

import com.eskgus.nammunity.domain.reports.Types;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.user.UsersListDto;
import lombok.Builder;
import lombok.Getter;

import static com.eskgus.nammunity.domain.enums.ContentType.COMMENTS;
import static com.eskgus.nammunity.domain.enums.ContentType.POSTS;

@Getter
public class ContentReportDetailDto <Dto> {
    private final String type;
    private PostsListDto postsListDto;
    private CommentsListDto commentsListDto;
    private UsersListDto usersListDto;
    private final ContentsPageDto<ContentReportDetailListDto> contentsPage;

    @Builder
    public ContentReportDetailDto(Types type, Dto dto,
                                  ContentsPageDto<ContentReportDetailListDto> contentsPage) {
        this.type = type.getDetail();
        generateDto(dto);
        this.contentsPage = contentsPage;
    }

    private void generateDto(Dto dto) {
        if (POSTS.getDetail().equals(type)) {
            this.postsListDto = (PostsListDto) dto;
        } else if (COMMENTS.getDetail().equals(type)) {
            this.commentsListDto = (CommentsListDto) dto;
        } else {
            this.usersListDto = (UsersListDto) dto;
        }
    }
}
