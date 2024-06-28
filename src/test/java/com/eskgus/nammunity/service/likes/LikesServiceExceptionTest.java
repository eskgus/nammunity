package com.eskgus.nammunity.service.likes;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.util.ServiceTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static com.eskgus.nammunity.util.ServiceTestUtil.assertIllegalArgumentException;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LikesServiceExceptionTest {
    @Mock
    private PostsService postsService;

    @Mock
    private CommentsService commentsService;

    @Mock
    private LikesRepository likesRepository;

    @Mock
    private PrincipalHelper principalHelper;

    @InjectMocks
    private LikesService likesService;

    private static final Long ID = 1L;

    @Test
    public void saveLikesWithAnonymousUser() {
        testSaveLikesThrowsPrincipalException(null);
    }

    @Test
    public void saveLikesWithNonExistentUsername() {
        Principal principal = mock(Principal.class);
        testSaveLikesThrowsPrincipalException(principal);
    }

    @Test
    public void savePostLikesWithNonExistentPost() {
        ExceptionMessages exceptionMessage = POST_NOT_FOUND;
        throwIllegalArgumentException(postsService::findById, exceptionMessage);

        testSaveLikesThrowsNotFoundException(ID, null, exceptionMessage);

        verifyFindMethods(ID, null, times(1), never());
    }

    @Test
    public void saveCommentLikesWithNonExistentComment() {
        ExceptionMessages exceptionMessage = COMMENT_NOT_FOUND;
        throwIllegalArgumentException(commentsService::findById, exceptionMessage);

        testSaveLikesThrowsNotFoundException(null, ID, exceptionMessage);

        verifyFindMethods(null, ID, never(), times(1));
    }

    @Test
    public void deleteLikesByContentIdWithAnonymousUser() {
        testDeleteLikesByContentIdThrowsPrincipalException(null);
    }

    @Test
    public void deleteLikesByContentIdWithNonExistentUsername() {
        Principal principal = mock(Principal.class);
        testDeleteLikesByContentIdThrowsPrincipalException(principal);
    }

    @Test
    public void deleteLikesByPostIdWithNonExistentPost() {
        ExceptionMessages exceptionMessage = POST_NOT_FOUND;
        throwIllegalArgumentException(postsService::findById, exceptionMessage);

        testDeleteLikesByContentIdThrowsNotFoundException(ID, null, exceptionMessage);

        verifyFindMethods(ID, null, times(1), never());
    }

    @Test
    public void deleteLikesByCommentIdWithNonExistentComment() {
        ExceptionMessages exceptionMessage = COMMENT_NOT_FOUND;
        throwIllegalArgumentException(commentsService::findById, exceptionMessage);

        testDeleteLikesByContentIdThrowsNotFoundException(null, ID, exceptionMessage);

        verifyFindMethods(null, ID, never(), times(1));
    }

    @Test
    public void deleteSelectedLikesWithEmptyLikeIds() {
        // given
        List<Long> likeIds = Collections.emptyList();

        // when/then
        testDeleteSelectedLikesException(likeIds, EMPTY_CONTENT_IDS, never());
    }

    @Test
    public void deleteSelectedLikesWithNonExistentLike() {
        // given
        List<Long> likeIds = Arrays.asList(ID, ID + 1, ID + 2);

        // when/then
        testDeleteSelectedLikesException(likeIds, LIKE_NOT_FOUND, times(1));
    }

    private Principal givePrincipal() {
        return ServiceTestUtil.givePrincipal(principalHelper::getUserFromPrincipal).getFirst();
    }

    private ExceptionMessages givePrincipalHelperThrowException(Principal principal) {
        ExceptionMessages exceptionMessage = principal == null ? UNAUTHORIZED : USERNAME_NOT_FOUND;
        ServiceTestUtil.throwIllegalArgumentException(
                principalHelper::getUserFromPrincipal, principal, true, exceptionMessage);

        return exceptionMessage;
    }

    private <T> void throwIllegalArgumentException(Function<Long, T> finder, ExceptionMessages exceptionMessage) {
        ServiceTestUtil.throwIllegalArgumentException(finder, exceptionMessage);
    }

    private void testSaveLikesThrowsPrincipalException(Principal principal) {
        // given
        ExceptionMessages exceptionMessage = givePrincipalHelperThrowException(principal);

        // when/then
        testSaveLikesException(ID, null, principal, exceptionMessage);

        verifyFindMethods(ID, null, never(), never());
    }

    private void testSaveLikesThrowsNotFoundException(Long postId, Long commentId, ExceptionMessages exceptionMessage) {
        // given
        Principal principal = givePrincipal();

        // when/then
        testSaveLikesException(postId, commentId, principal, exceptionMessage);
    }

    private void testSaveLikesException(Long postId, Long commentId, Principal principal, ExceptionMessages exceptionMessage) {
        assertIllegalArgumentException(() -> likesService.save(postId, commentId, principal), exceptionMessage);

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(likesRepository, never()).save(any(Likes.class));
    }

    private void testDeleteLikesByContentIdThrowsPrincipalException(Principal principal) {
        // given
        ExceptionMessages exceptionMessage = givePrincipalHelperThrowException(principal);

        // when/then
        testDeleteLikesByContentIdException(ID, null, principal, exceptionMessage);

        verifyFindMethods(ID, null, never(), never());
    }

    private void testDeleteLikesByContentIdThrowsNotFoundException(Long postId, Long commentId,
                                                                   ExceptionMessages exceptionMessage) {
        // given
        Principal principal = givePrincipal();

        // when/then
        testDeleteLikesByContentIdException(postId, commentId, principal, exceptionMessage);
    }

    private void testDeleteLikesByContentIdException(Long postId, Long commentId, Principal principal,
                                                     ExceptionMessages exceptionMessage) {
        assertIllegalArgumentException(
                () -> likesService.deleteByContentId(postId, commentId, principal), exceptionMessage);

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(likesRepository, never()).deleteByPosts(any(Posts.class), any(User.class));
        verify(likesRepository, never()).deleteByComments(any(Comments.class), any(User.class));
    }

    private void testDeleteSelectedLikesException(List<Long> likeIds,
                                                  ExceptionMessages exceptionMessage, VerificationMode mode) {
        assertIllegalArgumentException(() -> likesService.deleteSelectedLikes(likeIds), exceptionMessage);

        verify(likesRepository, mode).findById(anyLong());
        verify(likesRepository, never()).delete(any(Likes.class));
    }

    private void verifyFindMethods(Long postId, Long commentId, VerificationMode postMode, VerificationMode commentMode) {
        verify(postsService, postMode).findById(eq(postId));
        verify(commentsService, commentMode).findById(eq(commentId));
    }
}
