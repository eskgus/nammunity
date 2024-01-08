package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.reports.ContentReportsRepository;
import com.eskgus.nammunity.domain.user.BannedUsers;
import com.eskgus.nammunity.domain.user.BannedUsersRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.service.comments.CommentsSearchService;
import com.eskgus.nammunity.service.posts.PostsSearchService;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.pagination.PaginationDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.user.ActivityHistoryDto;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
import com.eskgus.nammunity.web.dto.user.UsersListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    public ActivityHistoryDto findActivityHistory(Long id, String type, int page) {
        User user = userRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException("존재하지 않는 회원입니다."));

        // pathVariable type이 posts든 comments든 게시글/댓글/신고 수 표시해야 함 ! (너무 많아서 map으로 넣는 거)
        Map<String, Long> numOfContents = new HashMap<>();
        numOfContents.put("postReports", contentReportsRepository.countPostReportsByUser(user));
        numOfContents.put("commentReports", contentReportsRepository.countCommentReportsByUser(user));
        numOfContents.put("userReports", contentReportsRepository.countUserReportsByUser(user));

        // BannedUsers에 없는 사용자면 bannedUser = null
        BannedUsers bannedUser = bannedUsersRepository.findByUser(user).orElse(null);

        Page<PostsListDto> posts = null;
        Page<CommentsListDto> comments = null;
        PaginationDto<?> paginationDto;
        // type에 따라 게시글/댓글 목록 추가
        if (type.equals("posts")) {
            posts = postsSearchService.findByUser(user, page, 10);
            numOfContents.put("comments", commentsSearchService.countByUser(user));

            // 페이지 번호
            paginationDto = PaginationDto.<PostsListDto>builder()
                    .page(posts).display(10).build();
        } else {
            comments = commentsSearchService.findByUser(user, page, 10);
            numOfContents.put("posts", postsSearchService.countByUser(user));

            // 페이지 번호
            paginationDto = PaginationDto.<CommentsListDto>builder()
                    .page(comments).display(10).build();
        }

        return ActivityHistoryDto.builder()
                .user(user).bannedUser(bannedUser).numOfContents(numOfContents)
                .posts(posts).comments(comments).paginationDto(paginationDto)
                .build();
    }

    @Transactional(readOnly = true)
    public List<UsersListDto> searchByNickname(String keywords) {
        return userRepository.searchByNickname(keywords).stream().map(UsersListDto::new).toList();
    }
}
