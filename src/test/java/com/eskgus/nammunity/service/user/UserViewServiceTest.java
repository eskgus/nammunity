package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.user.*;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.service.likes.LikesService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.service.reports.ReportsService;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageMoreDtos;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.user.ActivityHistoryDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserViewServiceTest {
    @Mock
    private UserService userService;

    @Mock
    private BannedUsersService bannedUsersService;

    @Mock
    private PostsService postsService;

    @Mock
    private CommentsService commentsService;

    @Mock
    private ReportsService reportsService;

    @Mock
    private LikesService likesService;

    @Mock
    private LikesRepository likesRepository;

    @Mock
    private PrincipalHelper principalHelper;

    @InjectMocks
    private UserViewService userViewService;

    @Test
    public void findPostsActivityHistory() {
        Page<PostsListDto> postsPage = new PageImpl<>(Collections.emptyList());
        when(postsService.findByUser(any(User.class), anyInt(), anyInt())).thenReturn(postsPage);

        long numberOfComments = 1;
        when(commentsService.countByUser(any(User.class))).thenReturn(numberOfComments);

        ActivityHistoryDto result = testFindActivityHistory(ContentType.POSTS.getDetailInEng());

        assertEquals(postsPage, result.getPostsHistoryDto().getContentsPage().getContents());
        assertEquals(numberOfComments, result.getPostsHistoryDto().getNumberOfComments());

        verify(postsService).findByUser(any(User.class), anyInt(), anyInt());
        verify(commentsService, never()).findByUser(any(User.class), anyInt(), anyInt());
        verify(commentsService).countByUser(any(User.class));
        verify(postsService, never()).countByUser(any(User.class));
    }

    @Test
    public void findCommentsActivityHistory() {
        Page<CommentsListDto> commentsPage = new PageImpl<>(Collections.emptyList());
        when(commentsService.findByUser(any(User.class), anyInt(), anyInt())).thenReturn(commentsPage);

        long numberOfPosts = 1;
        when(postsService.countByUser(any(User.class))).thenReturn(numberOfPosts);

        ActivityHistoryDto result = testFindActivityHistory(ContentType.COMMENTS.getDetailInEng());

        assertEquals(commentsPage, result.getCommentsHistoryDto().getContentsPage().getContents());
        assertEquals(numberOfPosts, result.getCommentsHistoryDto().getNumberOfPosts());

        verify(postsService, never()).findByUser(any(User.class), anyInt(), anyInt());
        verify(commentsService).findByUser(any(User.class), anyInt(), anyInt());
        verify(commentsService, never()).countByUser(any(User.class));
        verify(postsService).countByUser(any(User.class));
    }

    @Test
    public void findActivityHistoryWithNonExistentUserId() {
        // given
        when(userService.findById(anyLong())).thenThrow(IllegalArgumentException.class);

        Long id = 1L;
        String type = ContentType.POSTS.getDetailInEng();
        int page = 1;

        // when/then
        assertThrows(IllegalArgumentException.class, () -> userViewService.findActivityHistory(id, type, page));

        verify(userService).findById(eq(id));

        verify(bannedUsersService, never()).findByUser(any(User.class));
    }

    @Test
    public void getMyPage() {
        // given
        Principal principal = mock(Principal.class);
        User user = mock(User.class);
        when(principalHelper.getUserFromPrincipal(principal, true)).thenReturn(user);

        Page<PostsListDto> postsPage = new PageImpl<>(Collections.emptyList());
        when(postsService.findByUser(any(User.class), anyInt(), anyInt())).thenReturn(postsPage);

        Page<CommentsListDto> commentsPage = new PageImpl<>(Collections.emptyList());
        when(commentsService.findByUser(any(User.class), anyInt(), anyInt())).thenReturn(commentsPage);

        Page<LikesListDto> likesPage = new PageImpl<>(Collections.emptyList());
        when(likesService.findLikesByUser(any(User.class), any(BiFunction.class), anyInt(), anyInt())).thenReturn(likesPage);

        // when
        ContentsPageMoreDtos<PostsListDto, CommentsListDto, LikesListDto> result
                = userViewService.getMyPage(principal);

        // then
        assertEquals(postsPage, result.getContentsPageMore1().getContents());
        assertEquals(commentsPage, result.getContentsPageMore2().getContents());
        assertEquals(likesPage, result.getContentsPageMore3().getContents());

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(postsService).findByUser(eq(user), anyInt(), anyInt());
        verify(commentsService).findByUser(eq(user), anyInt(), anyInt());
        verify(likesService).findLikesByUser(eq(user), any(BiFunction.class), anyInt(), anyInt());
    }

    @Test
    public void getMyPageWithoutPrincipal() {
        // given
        when(principalHelper.getUserFromPrincipal(null, true))
                .thenThrow(IllegalArgumentException.class);

        // when/then
        assertThrows(IllegalArgumentException.class, () -> userViewService.getMyPage(null));

        verify(principalHelper).getUserFromPrincipal(null, true);

        verify(postsService, never()).findByUser(any(User.class), anyInt(), anyInt());
        verify(commentsService, never()).findByUser(any(User.class), anyInt(), anyInt());
        verify(likesService, never()).findLikesByUser(any(User.class), any(BiFunction.class), anyInt(), anyInt());
    }

    private ActivityHistoryDto testFindActivityHistory(String type) {
        // given
        LocalDateTime now = LocalDateTime.now();

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.getCreatedDate()).thenReturn(now);
        when(userService.findById(user.getId())).thenReturn(user);

        BannedUsers bannedUser = createMockedBannedUser(now);
        when(bannedUsersService.findByUser(any(User.class))).thenReturn(Optional.of(bannedUser));

        Map<String, Long> numberOfReports = createNumberOfReports();

        int page = 1;

        // when
        ActivityHistoryDto result = userViewService.findActivityHistory(user.getId(), type, page);

        // then
        assertEquals(bannedUser.getCount(), result.getBannedHistoryDto().getCount());
        result.getNumberOfReports().forEach(entry -> {
            String key = entry.getKey();
            Long value = entry.getValue();
            assertEquals(numberOfReports.get(key), value);
        });

        return result;
    }

    private BannedUsers createMockedBannedUser(LocalDateTime now) {
        BannedUsers bannedUser = mock(BannedUsers.class);
        when(bannedUser.getCount()).thenReturn(1);
        when(bannedUser.getPeriod()).thenReturn(Period.ofWeeks(1));
        when(bannedUser.getStartedDate()).thenReturn(now);
        when(bannedUser.getExpiredDate()).thenReturn(now.plusWeeks(1));

        return bannedUser;
    }

    private Map<String, Long> createNumberOfReports() {
        long numberOfPostReports = 1L;
        when(reportsService.countReportsByContentTypeAndUser(eq(ContentType.POSTS), any(User.class)))
                .thenReturn(numberOfPostReports);

        long numberOfCommentReports = 2L;
        when(reportsService.countReportsByContentTypeAndUser(eq(ContentType.COMMENTS), any(User.class)))
                .thenReturn(numberOfCommentReports);

        long numberOfUserReports = 3L;
        when(reportsService.countReportsByContentTypeAndUser(eq(ContentType.USERS), any(User.class)))
                .thenReturn(numberOfUserReports);

        return Map.of(
                ContentType.POSTS.getDetailInKor(), numberOfPostReports,
                ContentType.COMMENTS.getDetailInKor(), numberOfCommentReports,
                ContentType.USERS.getDetailInKor(), numberOfUserReports
        );
    }
}
