package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.reports.CommunityReportsRepository;
import com.eskgus.nammunity.domain.reports.Reasons;
import com.eskgus.nammunity.domain.reports.Types;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.comments.CommentsSearchService;
import com.eskgus.nammunity.service.posts.PostsSearchService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.reports.CommunityReportsSaveDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ReportsService {
    private final CommunityReportsRepository communityReportsRepository;
    private final UserService userService;
    private final PostsSearchService postsSearchService;
    private final CommentsSearchService commentsSearchService;
    private final ReasonsService reasonsService;
    private final TypesService typesService;

    @Transactional
    public Long saveCommunityReports(CommunityReportsSaveDto requestDto, String username) {
        User reporter = userService.findByUsername(username);
        Reasons reasons = reasonsService.findById(requestDto.getReasonsId());
        String otherReasons = requestDto.getOtherReasons();
        if (reasons.getDetail().equals("기타") && (otherReasons == null)) {
            throw new IllegalArgumentException("otherReasons");
        }

        Posts posts = null;
        Comments comments = null;
        User user = null;
        Types types;

        if (requestDto.getPostsId() != null) {
            posts = postsSearchService.findById(requestDto.getPostsId());
            types = typesService.findByDetail("게시글");
        } else if (requestDto.getCommentsId() != null) {
            comments = commentsSearchService.findById(requestDto.getCommentsId());
            types = typesService.findByDetail("댓글");
        } else if (requestDto.getUserId() != null){
            user = userService.findById(requestDto.getUserId());
            types = typesService.findByDetail("사용자");
        } else {
            throw new IllegalArgumentException("types");
        }

        CommunityReportsSaveDto communityReportsSaveDto = CommunityReportsSaveDto.builder()
                .posts(posts).comments(comments).user(user)
                .reporter(reporter).types(types)
                .reasons(reasons).otherReasons(otherReasons)
                .build();

        return communityReportsRepository.save(communityReportsSaveDto.toEntity()).getId();
    }
}
