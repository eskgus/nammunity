package com.eskgus.nammunity.service.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.likes.LikesService;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static com.eskgus.nammunity.util.ServiceExceptionTestUtil.assertIllegalArgumentException;
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

        User user = mock(User.class);
        when(user.getId()).thenReturn(ID);

        CommentsReadDto commentsReadDto = mock(CommentsReadDto.class);
        when(commentsReadDto.getAuthorId()).thenReturn(ID);
        when(commentsReadDto.getId()).thenReturn(ID);

        List<CommentsReadDto> content = Collections.singletonList(commentsReadDto);
        Page<CommentsReadDto> commentsPage = new PageImpl<>(content);
        when(commentsService.findByPosts(any(Posts.class), anyInt())).thenReturn(commentsPage);

        ExceptionMessages exceptionMessage = COMMENT_NOT_FOUND;
        when(commentsService.findById(anyLong()))
                .thenThrow(new IllegalArgumentException(exceptionMessage.getMessage()));

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
        when(principalHelper.getUserFromPrincipal(principal, true))
                .thenThrow(new IllegalArgumentException(exceptionMessage.getMessage()));

        // when/then
        assertIllegalArgumentException(() -> commentsViewService.listComments(principal, PAGE), exceptionMessage);

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(commentsService, never()).findByUser(any(User.class), eq(PAGE), anyInt());
    }
}
