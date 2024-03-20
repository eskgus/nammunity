package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.reports.ContentReportsRepository;
import com.eskgus.nammunity.domain.user.BannedUsersRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.service.comments.CommentsSearchService;
import com.eskgus.nammunity.service.posts.PostsSearchService;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.pagination.PaginationDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

        UsersListDto usersListDto = new UsersListDto(user);
        BannedHistoryDto bannedHistoryDto = getBannedHistoryDto(user);
        PostsHistoryDto postsHistoryDto = getPostsHistoryDto(type, user, page);
        CommentsHistoryDto commentsHistoryDto = getCommentsHistoryDto(type, user, page);
        Set<Map.Entry<String, Long>> numberOfReports = getNumberOfReports(user);

        return ActivityHistoryDto.builder()
                .usersListDto(usersListDto).bannedHistoryDto(bannedHistoryDto)
                .postsHistoryDto(postsHistoryDto).commentsHistoryDto(commentsHistoryDto)
                .numberOfReports(numberOfReports).build();
    }

    private BannedHistoryDto getBannedHistoryDto(User user) {
        return bannedUsersRepository.findByUser(user).map(BannedHistoryDto::new).orElse(null);
    }

    private PostsHistoryDto getPostsHistoryDto(String type, User user, int page) {
        if (!type.equals(ContentType.POSTS.getDetailInEng())) {
            return null;
        }

        Page<PostsListDto> posts = postsSearchService.findByUser(user, page, 10);
        PaginationDto<PostsListDto> pages = createPaginationDto(posts);
        long numberOfComments = commentsSearchService.countByUser(user);

        return PostsHistoryDto.builder()
                .posts(posts).pages(pages).numberOfComments(numberOfComments).build();
    }

    private <T> PaginationDto<T> createPaginationDto(Page<T> page) {
        return PaginationDto.<T>builder().page(page).display(10).build();
    }

    private CommentsHistoryDto getCommentsHistoryDto(String type, User user, int page) {
        if (!type.equals(ContentType.COMMENTS.getDetailInEng())) {
            return null;
        }

        Page<CommentsListDto> comments = commentsSearchService.findByUser(user, page, 10);
        PaginationDto<CommentsListDto> pages = createPaginationDto(comments);
        long numberOfPosts = postsSearchService.countByUser(user);

        return CommentsHistoryDto.builder()
                .comments(comments).pages(pages).numberOfPosts(numberOfPosts).build();
    }

    private Set<Map.Entry<String, Long>> getNumberOfReports(User user) {
        long numberOfPostReports = contentReportsRepository.countReportsByContentTypeAndUser(ContentType.POSTS, user);
        long numberOfCommentReports = contentReportsRepository.countReportsByContentTypeAndUser(ContentType.COMMENTS, user);
        long numberOfUserReports = contentReportsRepository.countReportsByContentTypeAndUser(ContentType.USERS, user);

        Map<String, Long> numberOfReports = new HashMap<>();
        numberOfReports.put(ContentType.POSTS.getDetailInKor(), numberOfPostReports);
        numberOfReports.put(ContentType.COMMENTS.getDetailInKor(), numberOfCommentReports);
        numberOfReports.put(ContentType.USERS.getDetailInKor(), numberOfUserReports);

        return numberOfReports.entrySet();
    }

    @Transactional(readOnly = true)
    public List<UsersListDto> searchByNickname(String keywords) {
        return userRepository.searchByNickname(keywords);
    }
}
