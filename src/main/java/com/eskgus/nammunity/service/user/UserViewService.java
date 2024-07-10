package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.service.likes.LikesService;
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
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.eskgus.nammunity.domain.enums.ContentType.COMMENTS;
import static com.eskgus.nammunity.domain.enums.ContentType.POSTS;

@RequiredArgsConstructor
@Service
public class UserViewService {
    private final UserService userService;
    private final BannedUsersService bannedUsersService;
    private final PostsService postsService;
    private final CommentsService commentsService;
    private final ReportsService reportsService;
    private final LikesService likesService;
    private final LikesRepository likesRepository;
    private final PrincipalHelper principalHelper;

    @Transactional(readOnly = true)
    public ActivityHistoryDto findActivityHistory(Long id, String type, int page) {
        User user = findUsersById(id);

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

    @Transactional(readOnly = true)
    public ContentsPageMoreDtos<PostsListDto, CommentsListDto, LikesListDto> getMyPage(Principal principal) {
        User user = getUserFromPrincipal(principal);

        int page = 1;
        int size = 5;

        Page<PostsListDto> postsPage = createPostsPage(user, page, size);
        ContentsPageMoreDto<PostsListDto> postsPageMoreDto = createContentsPageMoreDto(postsPage);

        Page<CommentsListDto> commentsPage = createCommentsPage(user, page, size);
        ContentsPageMoreDto<CommentsListDto> commentsPageMoreDto = createContentsPageMoreDto(commentsPage);

        Page<LikesListDto> likesPage = likesService.findLikesByUser(user, likesRepository::findByUser, page, size);
        ContentsPageMoreDto<LikesListDto> likesPageMoreDto = createContentsPageMoreDto(likesPage);

        return ContentsPageMoreDtos.<PostsListDto, CommentsListDto, LikesListDto>builder()
                .contentsPageMore1(postsPageMoreDto).contentsPageMore2(commentsPageMoreDto)
                .contentsPageMore3(likesPageMoreDto).build();
    }

    @Transactional(readOnly = true)
    public UserUpdateDto afterSignUp(Long id) {
        User user = findUsersById(id);
        return createUserUpdateDto(user);
    }

    @Transactional(readOnly = true)
    public UserUpdateDto updateUserInfo(Principal principal) {
        User user = getUserFromPrincipal(principal);
        return createUserUpdateDto(user);
    }

    private User getUserFromPrincipal(Principal principal) {
        return principalHelper.getUserFromPrincipal(principal, true);
    }

    private BannedHistoryDto getBannedHistoryDto(User user) {
        return bannedUsersService.findByUser(user).map(BannedHistoryDto::new).orElse(null);
    }

    private PostsHistoryDto getPostsHistoryDto(String type, User user, int page) {
        if (!POSTS.getName().equals(type)) {
            return null;
        }

        Page<PostsListDto> postsPage = createPostsPage(user, page, 10);
        ContentsPageDto<PostsListDto> postsPageDto = createContentsPageDto(postsPage);
        long numberOfComments = commentsService.countByUser(user);

        return PostsHistoryDto.builder()
                .contentsPage(postsPageDto).numberOfComments(numberOfComments).build();
    }

    private CommentsHistoryDto getCommentsHistoryDto(String type, User user, int page) {
        if (!COMMENTS.getName().equals(type)) {
            return null;
        }

        Page<CommentsListDto> commentsPage = createCommentsPage(user, page, 10);
        ContentsPageDto<CommentsListDto> commentsPageDto = createContentsPageDto(commentsPage);
        long numberOfPosts = postsService.countByUser(user);

        return CommentsHistoryDto.builder()
                .contentsPage(commentsPageDto).numberOfPosts(numberOfPosts).build();
    }

    private Set<Map.Entry<String, Long>> getNumberOfReports(User user) {
        long numberOfPostReports = countReportsByContentTypeAndUser(POSTS, user);
        long numberOfCommentReports = countReportsByContentTypeAndUser(ContentType.COMMENTS, user);
        long numberOfUserReports = countReportsByContentTypeAndUser(ContentType.USERS, user);

        Map<String, Long> numberOfReports = new HashMap<>();
        numberOfReports.put(POSTS.getDetail(), numberOfPostReports);
        numberOfReports.put(ContentType.COMMENTS.getDetail(), numberOfCommentReports);
        numberOfReports.put(ContentType.USERS.getDetail(), numberOfUserReports);

        return numberOfReports.entrySet();
    }

    private <Dto> ContentsPageMoreDto<Dto> createContentsPageMoreDto(Page<Dto> contentsPage) {
        return new ContentsPageMoreDto<>(contentsPage);
    }

    private User findUsersById(Long userId) {
        return userService.findById(userId);
    }

    private UserUpdateDto createUserUpdateDto(User user) {
        return new UserUpdateDto(user);
    }

    private <Dto> ContentsPageDto<Dto> createContentsPageDto(Page<Dto> page) {
        return new ContentsPageDto<>(page);
    }

    private Page<PostsListDto> createPostsPage(User user, int page, int size) {
        return postsService.findByUser(user, page, size);
    }

    private Page<CommentsListDto> createCommentsPage(User user, int page, int size) {
        return commentsService.findByUser(user, page, size);
    }

    private long countReportsByContentTypeAndUser(ContentType contentType, User user) {
        return reportsService.countReportsByContentTypeAndUser(contentType, user);
    }
}
