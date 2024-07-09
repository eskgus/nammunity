package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.enums.SocialType;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.user.*;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.service.likes.LikesService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.service.reports.ReportsService;
import com.eskgus.nammunity.util.ServiceTestUtil;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageMoreDtos;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.user.ActivityHistoryDto;
import com.eskgus.nammunity.web.dto.user.UserUpdateDto;
import org.assertj.core.util.TriFunction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import org.springframework.data.domain.Page;
import org.springframework.data.util.Pair;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.eskgus.nammunity.domain.enums.ContentType.*;
import static com.eskgus.nammunity.domain.enums.SocialType.*;
import static org.junit.jupiter.api.Assertions.*;
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

    private static final int PAGE = 1;

    @Test
    public void findPostsActivityHistory() {
        User user = testFindActivityHistory(POSTS, postsService::findByUser, commentsService::countByUser);

        verifyFindActivityHistory(times(1), never(), user);
    }

    @Test
    public void findCommentsActivityHistory() {
        User user = testFindActivityHistory(COMMENTS, commentsService::findByUser, postsService::countByUser);

        verifyFindActivityHistory(never(), times(1), user);
    }

    @Test
    public void getMyPage() {
        // given
        Pair<Principal, User> pair = givePrincipal();
        Principal principal = pair.getFirst();
        User user = pair.getSecond();

        Page<PostsListDto> postsPage = giveContentsPage(postsService::findByUser);

        Page<CommentsListDto> commentsPage = giveContentsPage(commentsService::findByUser);

        Page<LikesListDto> likesPage = ServiceTestUtil.createContentsPage();
        when(likesService.findLikesByUser(any(User.class), any(BiFunction.class), anyInt(), anyInt()))
                .thenReturn(likesPage);

        // when
        ContentsPageMoreDtos<PostsListDto, CommentsListDto, LikesListDto> result = userViewService.getMyPage(principal);

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
    public void afterSignUp() {
        // given
        User user = giveUserId();

        giveSocial(user, NONE);

        // when
        UserUpdateDto result = userViewService.afterSignUp(user.getId());

        // then
        assertEquals(user.getId(), result.getId());

        verify(userService).findById(eq(user.getId()));
    }

    @Test
    public void updateUserInfoWithRegularUser() {
        testUpdateUserInfo(NONE);
    }

    @Test
    public void updateUserInfoWithGoogleUser() {
        testUpdateUserInfo(GOOGLE);
    }

    @Test
    public void updateUserInfoWithNaverUser() {
        testUpdateUserInfo(NAVER);
    }

    @Test
    public void updateUserInfoWithKakaoUser() {
        testUpdateUserInfo(KAKAO);
    }

    private <Dto> User testFindActivityHistory(ContentType contentType,
                                               TriFunction<User, Integer, Integer, Page<Dto>> finder,
                                               Function<User, Long> counter) {
        // given
        User user = giveUserId();

        String type = contentType.getName();

        when(user.getCreatedDate()).thenReturn(LocalDateTime.now());

        BannedUsers bannedUser = giveBannedUser();
        ServiceTestUtil.giveContentFinder(bannedUsersService::findByUser, User.class, bannedUser);

        Page<Dto> contentsPage = giveContentsPage(finder);

        long numberOfContents = giveCountContents(counter);

        long numberOfReports = giveCountReports();

        // when
        ActivityHistoryDto result = userViewService.findActivityHistory(user.getId(), type, PAGE);

        // then
        assertEquals(user.getId(), result.getUsersListDto().getId());
        assertEquals(bannedUser.getCount(), result.getBannedHistoryDto().getCount());
        assertNumberOfReports(numberOfReports, result.getNumberOfReports());
        assertContentsPage(contentType, result, contentsPage);
        assertNumberOfContents(contentType, result, numberOfContents);

        verify(userService).findById(eq(user.getId()));
        verify(bannedUsersService).findByUser(eq(user));
        verifyCountReportsByContentTypeAndUser(user);

        return user;
    }

    private void testUpdateUserInfo(SocialType socialType) {
        // given
        Pair<Principal, User> pair = givePrincipal();
        Principal principal = pair.getFirst();
        User user = pair.getSecond();

        giveSocial(user, socialType);

        // when
        UserUpdateDto result = userViewService.updateUserInfo(principal);

        // then
        assertEquals(NONE.equals(socialType), result.isNone());
        assertEquals(GOOGLE.equals(socialType), result.isGoogle());
        assertEquals(NAVER.equals(socialType), result.isNaver());
        assertEquals(KAKAO.equals(socialType), result.isKakao());

        verify(principalHelper).getUserFromPrincipal(principal, true);
    }

    private User giveUserId() {
        return ServiceTestUtil.giveUserId(1L, userService::findById);
    }

    private BannedUsers giveBannedUser() {
        LocalDateTime now = LocalDateTime.now();

        BannedUsers bannedUser = mock(BannedUsers.class);
        when(bannedUser.getCount()).thenReturn(1);
        when(bannedUser.getPeriod()).thenReturn(Period.ofWeeks(1));
        when(bannedUser.getStartedDate()).thenReturn(now);
        when(bannedUser.getExpiredDate()).thenReturn(now.plusWeeks(1));

        return bannedUser;
    }

    private <Dto> Page<Dto> giveContentsPage(TriFunction<User, Integer, Integer, Page<Dto>> finder) {
        return ServiceTestUtil.giveContentsPage(finder, User.class);
    }

    private long giveCountContents(Function<User, Long> counter) {
        long numberOfContents = 1L;
        when(counter.apply(any(User.class))).thenReturn(numberOfContents);

        return numberOfContents;
    }

    private long giveCountReports() {
        long numberOfReports = 10L;
        when(reportsService.countReportsByContentTypeAndUser(any(ContentType.class), any(User.class)))
                .thenReturn(numberOfReports);

        return numberOfReports;
    }

    private Pair<Principal, User> givePrincipal() {
        return ServiceTestUtil.givePrincipal(principalHelper::getUserFromPrincipal);
    }

    private void giveSocial(User user, SocialType socialType) {
        when(user.getSocial()).thenReturn(socialType);
    }

    private void assertNumberOfReports(long numberOfReports, Set<Map.Entry<String, Long>> result) {
        result.forEach(entry -> {
            Long value = entry.getValue();

            assertEquals(numberOfReports, value);
        });
    }

    private <Dto> void assertContentsPage(ContentType contentType, ActivityHistoryDto result,
                                          Page<Dto> contentsPage) {
        Page<?> actualContentsPage = POSTS.equals(contentType)
                ? result.getPostsHistoryDto().getContentsPage().getContents()
                : result.getCommentsHistoryDto().getContentsPage().getContents();

        assertEquals(contentsPage, actualContentsPage);
    }

    private void assertNumberOfContents(ContentType contentType, ActivityHistoryDto result, long numberOfContents) {
        long actualNumberOfContents = POSTS.equals(contentType)
                ? result.getPostsHistoryDto().getNumberOfComments()
                : result.getCommentsHistoryDto().getNumberOfPosts();

        assertEquals(numberOfContents, actualNumberOfContents);
    }

    private void verifyCountReportsByContentTypeAndUser(User user) {
        for (ContentType contentType : ContentType.values()) {
            verify(reportsService).countReportsByContentTypeAndUser(eq(contentType), eq(user));
        }
    }

    private void verifyFindActivityHistory(VerificationMode postMode, VerificationMode commentMode, User user) {
        verify(postsService, postMode).findByUser(eq(user), eq(PAGE), anyInt());
        verify(commentsService, postMode).countByUser(eq(user));
        verify(commentsService, commentMode).findByUser(eq(user), eq(PAGE), anyInt());
        verify(postsService, commentMode).countByUser(eq(user));
    }
}
