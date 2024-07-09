package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.service.likes.LikesService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.service.reports.ReportsService;
import com.eskgus.nammunity.util.ServiceTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.util.function.BiFunction;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserViewServiceExceptionTest {
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

    private static final Long ID = 1L;

    @Test
    public void findActivityHistoryWithNonExistentUser() {
        // given
        throwIllegalArgumentException();

        // when/then
        assertIllegalArgumentException(
                () -> userViewService.findActivityHistory(ID, "type", 1), USER_NOT_FOUND);

        verify(userService).findById(eq(ID));
        verify(bannedUsersService, never()).findByUser(any(User.class));
        verify(postsService, never()).findByUser(any(User.class), anyInt(), anyInt());
        verify(commentsService, never()).countByUser(any(User.class));
        verify(commentsService, never()).findByUser(any(User.class), anyInt(), anyInt());
        verify(postsService, never()).countByUser(any(User.class));
        verify(reportsService, never()).countReportsByContentTypeAndUser(any(ContentType.class), any(User.class));
    }

    @Test
    public void getMyPageWithAnonymousUser() {
        testGetMyPageException(null, UNAUTHORIZED);
    }

    @Test
    public void getMyPageWithNonExistentUsername() {
        Principal principal = mock(Principal.class);
        testGetMyPageException(principal, USERNAME_NOT_FOUND);
    }

    @Test
    public void afterSignUpWithNonExistentUser() {
        // given
        throwIllegalArgumentException();

        // when/then
        assertIllegalArgumentException(() -> userViewService.afterSignUp(ID), USER_NOT_FOUND);

        verify(userService).findById(eq(ID));
    }

    @Test
    public void updateUserInfoWithAnonymousUser() {
        testUpdateUserInfo(null, UNAUTHORIZED);
    }

    @Test
    public void updateUserInfoWithNonExistentUsername() {
        Principal principal = mock(Principal.class);
        testUpdateUserInfo(principal, USERNAME_NOT_FOUND);
    }

    private void testGetMyPageException(Principal principal, ExceptionMessages exceptionMessage) {
        // given
        throwIllegalArgumentException(principal, exceptionMessage);

        // when/then
        assertIllegalArgumentException(() -> userViewService.getMyPage(principal), exceptionMessage);

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(postsService, never()).findByUser(any(User.class), anyInt(), anyInt());
        verify(commentsService, never()).findByUser(any(User.class), anyInt(), anyInt());
        verify(likesService, never()).findLikesByUser(any(User.class), any(BiFunction.class), anyInt(), anyInt());
    }

    private void testUpdateUserInfo(Principal principal, ExceptionMessages exceptionMessage) {
        // given
        throwIllegalArgumentException(principal, exceptionMessage);

        // when/then
        assertIllegalArgumentException(() -> userViewService.updateUserInfo(principal), exceptionMessage);

        verify(principalHelper).getUserFromPrincipal(principal, true);
    }

    private void throwIllegalArgumentException() {
        ServiceTestUtil.throwIllegalArgumentException(userService::findById, USER_NOT_FOUND);
    }

    private void throwIllegalArgumentException(Principal principal, ExceptionMessages exceptionMessage) {
        ServiceTestUtil.throwIllegalArgumentException(
                principalHelper::getUserFromPrincipal, principal, true, exceptionMessage);
    }

    private void assertIllegalArgumentException(Executable executable, ExceptionMessages exceptionMessage) {
        ServiceTestUtil.assertIllegalArgumentException(executable, exceptionMessage);
    }
}
