package com.eskgus.nammunity.service.posts;

import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.comments.CommentsViewService;
import com.eskgus.nammunity.service.likes.LikesService;
import com.eskgus.nammunity.service.reports.ReasonsService;
import com.eskgus.nammunity.util.ServiceTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;

import java.security.Principal;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static com.eskgus.nammunity.util.ServiceTestUtil.assertIllegalArgumentException;
import static com.eskgus.nammunity.util.ServiceTestUtil.giveUser;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostsViewServiceExceptionTest {
    @Mock
    private PostsService postsService;

    @Mock
    private ReasonsService reasonsService;

    @Mock
    private LikesService likesService;

    @Mock
    private CommentsViewService commentsViewService;

    @Mock
    private PrincipalHelper principalHelper;

    @InjectMocks
    private PostsViewService postsViewService;

    private static final Long ID = 1L;
    private static final int PAGE = 1;

    @Test
    public void readPostsWithNonExistentPost() {
        // given
        Posts post = givePost(false);

        Principal principal = mock(Principal.class);

        ExceptionMessages exceptionMessage = throwIllegalArgumentException();

        // when/then
        testReadPostsException(post.getId(), principal, exceptionMessage, never());
    }

    @Test
    public void readPostsWithNonExistentUsername() {
        // given
        Posts post = givePost(true);

        Principal principal = mock(Principal.class);

        User user = giveUser(ID);
        when(post.getUser()).thenReturn(user);

        ExceptionMessages exceptionMessage = USERNAME_NOT_FOUND;
        throwIllegalArgumentException(principal, false, exceptionMessage);

        // when/then
        testReadPostsException(post.getId(), principal, exceptionMessage, times(1));
    }

    @Test
    public void readCommentsWithNonExistentPost() {
        // given
        Posts post = givePost(false);

        Principal principal = mock(Principal.class);

        ExceptionMessages exceptionMessage = throwIllegalArgumentException();

        // when/then
        testReadCommentsThrowsNotFoundPostOrUsernameException(post, principal, exceptionMessage, never());
    }

    @Test
    public void readCommentsWithNonExistentUsername() {
        // given
        Posts post = givePost(true);

        Principal principal = mock(Principal.class);

        ExceptionMessages exceptionMessage = USERNAME_NOT_FOUND;
        throwIllegalArgumentException(principal, false, exceptionMessage);

        // when/then
        testReadCommentsThrowsNotFoundPostOrUsernameException(
                post, principal, exceptionMessage, times(1));
    }

    @Test
    public void readCommentsWithNonExistentComment() {
        // given
        Posts post = givePost(true);

        Principal principal = mock(Principal.class);

        when(principalHelper.getUserFromPrincipal(principal, false)).thenReturn(null);

        ExceptionMessages exceptionMessage = COMMENT_NOT_FOUND;
        when(commentsViewService.findCommentsPageByPosts(any(Posts.class), isNull(), anyInt()))
                .thenThrow(new IllegalArgumentException(exceptionMessage.getMessage()));

        // when/then
        testReadCommentsException(post, principal, exceptionMessage, times(1));

        verify(commentsViewService).findCommentsPageByPosts(eq(post), isNull(), eq(PAGE));
    }

    @Test
    public void listPostsWithAnonymousUser() {
        testListPostsException(null, UNAUTHORIZED);
    }

    @Test
    public void listPostsWithNonExistentUsername() {
        Principal principal = mock(Principal.class);
        testListPostsException(principal, USERNAME_NOT_FOUND);
    }

    @Test
    public void updatePostsWithNonExistentPost() {
        // given
        ExceptionMessages exceptionMessage = throwIllegalArgumentException();

        // when/then
        assertIllegalArgumentException(() -> postsViewService.updatePosts(ID), exceptionMessage);

        verify(postsService).findById(eq(ID));
    }

    private Posts givePost(boolean findPosts) {
        if (findPosts) {
            return ServiceTestUtil.givePost(ID, postsService::findById);
        }
        return ServiceTestUtil.givePost(ID);
    }

    private ExceptionMessages throwIllegalArgumentException() {
        ExceptionMessages exceptionMessage = POST_NOT_FOUND;
        ServiceTestUtil.throwIllegalArgumentException(postsService::findById, exceptionMessage);

        return exceptionMessage;
    }

    private void throwIllegalArgumentException(Principal principal, boolean throwExceptionOnMissingPrincipal,
                                               ExceptionMessages exceptionMessage) {
        ServiceTestUtil.throwIllegalArgumentException(
                principalHelper::getUserFromPrincipal, principal, throwExceptionOnMissingPrincipal, exceptionMessage);
    }

    private void testReadPostsException(Long postId, Principal principal, ExceptionMessages exceptionMessage,
                                        VerificationMode mode) {
        assertIllegalArgumentException(() -> postsViewService.readPosts(postId, principal), exceptionMessage);

        verify(postsService).findById(eq(postId));
        verify(principalHelper, mode).getUserFromPrincipal(principal, false);
        verify(postsService, never()).countView(any(Posts.class));
        verify(likesService, never()).existsByPostsAndUser(any(Posts.class), any(User.class));
        verify(reasonsService, never()).findAllAsc();
    }

    private void testReadCommentsThrowsNotFoundPostOrUsernameException(Posts post, Principal principal,
                                                                       ExceptionMessages exceptionMessage,
                                                                       VerificationMode mode) {
        testReadCommentsException(post, principal, exceptionMessage, mode);

        verify(commentsViewService, never()).findCommentsPageByPosts(eq(post), any(User.class), eq(PAGE));
    }

    private void testReadCommentsException(Posts post, Principal principal, ExceptionMessages exceptionMessage,
                                           VerificationMode mode) {
        assertIllegalArgumentException(
                () -> postsViewService.readComments(post.getId(), principal, PAGE), exceptionMessage);

        verify(postsService).findById(eq(post.getId()));
        verify(principalHelper, mode).getUserFromPrincipal(principal, false);
    }

    private void testListPostsException(Principal principal, ExceptionMessages exceptionMessage) {
        // given
        throwIllegalArgumentException(principal, true, exceptionMessage);

        // when/then
        assertIllegalArgumentException(() -> postsViewService.listPosts(principal, PAGE), exceptionMessage);

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(postsService, never()).findByUser(any(User.class), eq(PAGE), anyInt());
    }
}
