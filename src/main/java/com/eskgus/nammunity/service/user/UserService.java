package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.reports.ContentReportsRepository;
import com.eskgus.nammunity.domain.user.BannedUsers;
import com.eskgus.nammunity.domain.user.BannedUsersRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.service.comments.CommentsSearchService;
import com.eskgus.nammunity.service.posts.PostsSearchService;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.user.ActivityHistoryDto;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PostsSearchService postsSearchService;
    private final CommentsSearchService commentsSearchService;
    private final BannedUsersRepository bannedUsersRepository;
    private final ContentReportsRepository contentReportsRepository;

    @Transactional
    public Long signUp(RegistrationDto registrationDto) {
        return userRepository.save(registrationDto.toEntity()).getId();
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new
                IllegalArgumentException("존재하지 않는 ID입니다."));
    }

    @Transactional
    public void resetAttempt(User user) {
        user.resetAttempt();
    }

    @Transactional(readOnly = true)
    public ActivityHistoryDto findActivityHistory(Long id, String type) {
        User user = userRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException("존재하지 않는 회원입니다."));

        // pathVariable type이 posts든 comments든 게시글/댓글/신고 수 표시해야 함 ! (너무 많아서 map으로 넣는 거)
        Map<String, Long> numOfContents = new HashMap<>();
        numOfContents.put("posts", postsSearchService.countByUser(user));
        numOfContents.put("comments", commentsSearchService.countByUser(user));
        numOfContents.put("postReports", contentReportsRepository.countPostReportsByUser(user));
        numOfContents.put("commentReports", contentReportsRepository.countCommentReportsByUser(user));
        numOfContents.put("userReports", contentReportsRepository.countUserReportsByUser(user));

        // BannedUsers에 없는 사용자면 bannedUser = null
        BannedUsers bannedUser = bannedUsersRepository.findByUser(user).orElse(null);

        List<PostsListDto> posts = null;
        List<CommentsListDto> comments = null;
        // type에 따라 List<PostsListDto>, List<CommentsListDto>에 값 넣기
        if (type.equals("posts")) {
            posts = postsSearchService.findByUser(user);
        } else {
            comments = commentsSearchService.findByUser(user);
        }

        return ActivityHistoryDto.builder()
                .user(user).bannedUser(bannedUser).numOfContents(numOfContents)
                .posts(posts).comments(comments)
                .build();
    }
}
