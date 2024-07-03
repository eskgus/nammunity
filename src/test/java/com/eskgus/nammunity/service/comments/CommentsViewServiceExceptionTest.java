package com.eskgus.nammunity.service.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.likes.LikesService;
import com.eskgus.nammunity.util.ServiceTestUtil;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentsViewServiceExceptionTest {
    @Mock
    private CommentsService commentsService;

    @Mock
    private LikesService likesService;

    @Mock
    private PrincipalHelper principalHelper;

    @InjectMocks
    private CommentsViewService commentsViewService;

    private static final Long ID = 1L;
    private static final int PAGE = 1;

    @Test
    public void findCommentsPageByPostsWithNonExistentComment() {
        // given
        Posts post = mock(Posts.class);

        User user = ServiceTestUtil.giveUserId(ID);

        CommentsReadDto commentsReadDto = mock(CommentsReadDto.class);
        when(commentsReadDto.getAuthorId()).thenReturn(ID);
        when(commentsReadDto.getId()).thenReturn(ID);

        ServiceTestUtil.giveContentsPage(commentsService::findByPosts, commentsReadDto);

        ExceptionMessages exceptionMessage = COMMENT_NOT_FOUND;
        ServiceTestUtil.throwIllegalArgumentException(commentsService::findById, exceptionMessage);

        // when/then
        assertIllegalArgumentException(
                () -> commentsViewService.findCommentsPageByPosts(post, user, PAGE), exceptionMessage);

        verify(commentsService).findByPosts(eq(post), eq(PAGE));
        verify(commentsReadDto).setDoesUserWriteComment(anyBoolean());
        verify(commentsService).findById(eq(commentsReadDto.getId()));
        verify(likesService, never()).existsByCommentsAndUser(any(Comments.class), eq(user));
        verify(commentsReadDto, never()).setDoesUserLikeComment(anyBoolean());
    }

    @Test
    public void listCommentsWithAnonymousUser() {
        testListCommentsException(null, UNAUTHORIZED);
    }

    @Test
    public void listCommentsWithNonExistentUsername() {
        Principal principal = mock(Principal.class);
        testListCommentsException(principal, USERNAME_NOT_FOUND);
    }

    private void testListCommentsException(Principal principal, ExceptionMessages exceptionMessage) {
        // given
        ServiceTestUtil.throwIllegalArgumentException(
                principalHelper::getUserFromPrincipal, principal, true, exceptionMessage);

        // when/then
        assertIllegalArgumentException(() -> commentsViewService.listComments(principal, PAGE), exceptionMessage);

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(commentsService, never()).findByUser(any(User.class), eq(PAGE), anyInt());
    }

    private void assertIllegalArgumentException(Executable executable, ExceptionMessages exceptionMessage) {
        ServiceTestUtil.assertIllegalArgumentException(executable, exceptionMessage);
    }
}
