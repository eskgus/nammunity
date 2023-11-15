package com.eskgus.nammunity.web.dto.user;

import com.eskgus.nammunity.domain.user.BannedUsers;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.util.DateTimeUtil;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class ActivityHistoryDto {
    private Long userId;
    private String nickname;
    private String createdDate; // 가입일

    // 누적 정지 횟수
    private Boolean bannedUsersExistence;   // BannedUsers에 없는 사용자면 화면에서 count만 0으로 표시
    private int count;
    private String period;
    private String startedDate;
    private String expiredDate;

    // 작성 글/댓글
    private List<PostsListDto> posts;
    private List<CommentsListDto> comments;

    // 작성 글/댓글 수
    private long numOfPosts;
    private long numOfComments;

    // 누적 신고 횟수
    private long numOfPostReports;
    private long numOfCommentReports;
    private long numOfUserReports;

    @Builder
    public ActivityHistoryDto(User user, BannedUsers bannedUser, Map<String, Long> numOfContents,
                              List<PostsListDto> posts, List<CommentsListDto> comments) {
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.createdDate = DateTimeUtil.formatDateTime(user.getCreatedDate());
        this.numOfPosts = numOfContents.get("posts");
        this.numOfComments = numOfContents.get("comments");
        this.numOfPostReports = numOfContents.get("postReports");
        this.numOfCommentReports = numOfContents.get("commentReports");
        this.numOfUserReports = numOfContents.get("userReports");

        if (bannedUser != null) {
            this.bannedUsersExistence = true;
            this.count = bannedUser.getCount();
            this.period = DateTimeUtil.convertPeriodToString(bannedUser.getPeriod());
            this.startedDate = DateTimeUtil.formatDateTime(bannedUser.getStartedDate());
            this.expiredDate = DateTimeUtil.formatDateTime(bannedUser.getExpiredDate());
        }

        if ((posts != null) && !posts.isEmpty()) {
            this.posts = posts;
        } else if ((comments != null) && !comments.isEmpty()) {
            this.comments = comments;
        }
    }
}
