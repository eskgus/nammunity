package com.eskgus.nammunity.web.dto.user;

import lombok.Builder;
import lombok.Getter;

import java.util.*;

@Getter
public class ActivityHistoryDto {
    private UsersListDto usersListDto;
    private BannedHistoryDto bannedHistoryDto;
    private PostsHistoryDto postsHistoryDto;
    private CommentsHistoryDto commentsHistoryDto;
    Set<Map.Entry<String, Long>> numberOfReports;

    @Builder
    public ActivityHistoryDto(UsersListDto usersListDto, BannedHistoryDto bannedHistoryDto,
                              PostsHistoryDto postsHistoryDto, CommentsHistoryDto commentsHistoryDto,
                              Set<Map.Entry<String, Long>> numberOfReports) {
        this.usersListDto = usersListDto;
        this.bannedHistoryDto = bannedHistoryDto;
        this.postsHistoryDto = postsHistoryDto;
        this.commentsHistoryDto = commentsHistoryDto;
        this.numberOfReports = numberOfReports;
    }
}
