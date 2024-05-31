package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.comments.CommentsSearchService;
import com.eskgus.nammunity.service.likes.LikesSearchService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.service.reports.ReportsService;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageMoreDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageMoreDtos;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class UserViewService {
    private final UserService userService;
    private final BannedUsersService bannedUsersService;
    private final PostsService postsService;
    private final CommentsSearchService commentsSearchService;
    private final ReportsService reportsService;
    private final LikesSearchService likesSearchService;
    private final LikesRepository likesRepository;

    @Autowired
    private PrincipalHelper principalHelper;

    @Transactional(readOnly = true)
    public ActivityHistoryDto findActivityHistory(Long id, String type, int page) {
        User user = userService.findById(id);

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
        return bannedUsersService.findByUser(user).map(BannedHistoryDto::new).orElse(null);
    }

    private PostsHistoryDto getPostsHistoryDto(String type, User user, int page) {
        if (!type.equals(ContentType.POSTS.getDetailInEng())) {
            return null;
        }

        Page<PostsListDto> contents = postsService.findByUser(user, page, 10);
        ContentsPageDto<PostsListDto> postsPage = createContentsPageDto(contents);
        long numberOfComments = commentsSearchService.countByUser(user);

        return PostsHistoryDto.builder()
                .contentsPage(postsPage).numberOfComments(numberOfComments).build();
    }

    private <T> ContentsPageDto<T> createContentsPageDto(Page<T> page) {
        return new ContentsPageDto<>(page);
    }

    private CommentsHistoryDto getCommentsHistoryDto(String type, User user, int page) {
        if (!type.equals(ContentType.COMMENTS.getDetailInEng())) {
            return null;
        }

        Page<CommentsListDto> contents = commentsSearchService.findByUser(user, page, 10);
        ContentsPageDto<CommentsListDto> commentsPage = createContentsPageDto(contents);
        long numberOfPosts = postsService.countByUser(user);

        return CommentsHistoryDto.builder()
                .contentsPage(commentsPage).numberOfPosts(numberOfPosts).build();
    }

    private Set<Map.Entry<String, Long>> getNumberOfReports(User user) {
        long numberOfPostReports = reportsService.countReportsByContentTypeAndUser(ContentType.POSTS, user);
        long numberOfCommentReports = reportsService.countReportsByContentTypeAndUser(ContentType.COMMENTS, user);
        long numberOfUserReports = reportsService.countReportsByContentTypeAndUser(ContentType.USERS, user);

        Map<String, Long> numberOfReports = new HashMap<>();
        numberOfReports.put(ContentType.POSTS.getDetailInKor(), numberOfPostReports);
        numberOfReports.put(ContentType.COMMENTS.getDetailInKor(), numberOfCommentReports);
        numberOfReports.put(ContentType.USERS.getDetailInKor(), numberOfUserReports);

        return numberOfReports.entrySet();
    }

    @Transactional(readOnly = true)
    public ContentsPageMoreDtos<PostsListDto, CommentsListDto, LikesListDto> getMyPage(Principal principal) {
        User user = principalHelper.getUserFromPrincipal(principal, true);

        int page = 1;
        int size = 5;

        Page<PostsListDto> postsPage = postsService.findByUser(user, page, size);
        ContentsPageMoreDto<PostsListDto> postsPageMoreDto = new ContentsPageMoreDto<>(postsPage);

        Page<CommentsListDto> commentsPage = commentsSearchService.findByUser(user, page, size);
        ContentsPageMoreDto<CommentsListDto> commentsPageMoreDto = new ContentsPageMoreDto<>(commentsPage);

        Page<LikesListDto> likesPage = likesSearchService.findLikesByUser(user, likesRepository::findByUser, page, size);
        ContentsPageMoreDto<LikesListDto> likesPageMoreDto = new ContentsPageMoreDto<>(likesPage);

        return ContentsPageMoreDtos.<PostsListDto, CommentsListDto, LikesListDto>builder()
                .contentsPageMore1(postsPageMoreDto).contentsPageMore2(commentsPageMoreDto)
                .contentsPageMore3(likesPageMoreDto).build();
    }

    @Transactional(readOnly = true)
    public UserUpdateDto afterSignUp(Long id) {
        User user = userService.findById(id);
        return new UserUpdateDto(user);
    }

    @Transactional(readOnly = true)
    public UserUpdateDto updateUserInfo(Principal principal) {
        User user = principalHelper.getUserFromPrincipal(principal, true);
        return new UserUpdateDto(user);
    }
}
