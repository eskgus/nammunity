package com.eskgus.nammunity.service.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.likes.LikesService;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.security.Principal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentsViewServiceTest {
    @Mock
    private CommentsService commentsService;

    @Mock
    private LikesService likesService;

    @Mock
    private PrincipalHelper principalHelper;

    @InjectMocks
    private CommentsViewService commentsViewService;

    @Test
    public void findCommentsPageByPosts() {
        // given
        CommentsReadDto commentsReadDto = mock(CommentsReadDto.class);
        when(commentsReadDto.getAuthorId()).thenReturn(1L);
        when(commentsReadDto.getId()).thenReturn(1L);

        List<CommentsReadDto> comments = Collections.singletonList(commentsReadDto);

        Page<CommentsReadDto> commentsPage = new PageImpl<>(comments);
        when(commentsService.findByPosts(any(Posts.class), anyInt())).thenReturn(commentsPage);

        Comments comment = mock(Comments.class);
        when(commentsService.findById(anyLong())).thenReturn(comment);

        when(likesService.existsByCommentsAndUser(any(Comments.class), any(User.class))).thenReturn(false);

        Posts post = mock(Posts.class);

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        int page = 1;

        // when
        Page<CommentsReadDto> result = commentsViewService.findCommentsPageByPosts(post, user, page);

        // then
        assertEquals(commentsPage, result);

        verify(commentsService).findByPosts(eq(post), eq(page));
        verify(commentsReadDto).setDoesUserWriteComment(true);
        verify(commentsService).findById(eq(commentsReadDto.getId()));

        verify(likesService).existsByCommentsAndUser(eq(comment), eq(user));
        verify(commentsReadDto).setDoesUserLikeComment(false);
    }

    @Test
    public void findCommentsPageByPostsWithNonExistentCommentId() {
        // given
        CommentsReadDto commentsReadDto = mock(CommentsReadDto.class);
        when(commentsReadDto.getAuthorId()).thenReturn(1L);
        when(commentsReadDto.getId()).thenReturn(1L);

        List<CommentsReadDto> comments = Collections.singletonList(commentsReadDto);

        Page<CommentsReadDto> commentsPage = new PageImpl<>(comments);
        when(commentsService.findByPosts(any(Posts.class), anyInt())).thenReturn(commentsPage);

        when(commentsService.findById(anyLong())).thenThrow(IllegalArgumentException.class);

        Posts post = mock(Posts.class);

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        int page = 1;

        // when/then
        assertThrows(IllegalArgumentException.class, () -> commentsViewService.findCommentsPageByPosts(post, user, page));

        verify(commentsService).findByPosts(eq(post), eq(page));
        verify(commentsReadDto).setDoesUserWriteComment(true);
        verify(commentsService).findById(eq(commentsReadDto.getId()));

        verify(likesService, never()).existsByCommentsAndUser(any(Comments.class), any(User.class));
        verify(commentsReadDto, never()).setDoesUserLikeComment(anyBoolean());
    }

    @Test
    public void listComments() {
        // given
        Principal principal = mock(Principal.class);
        User user = mock(User.class);
        when(principalHelper.getUserFromPrincipal(principal, true)).thenReturn(user);

        Page<CommentsListDto> commentsPage = new PageImpl<>(Collections.emptyList());
        when(commentsService.findByUser(any(User.class), anyInt(), anyInt())).thenReturn(commentsPage);

        int page = 1;

        // when
        ContentsPageDto<CommentsListDto> result = commentsViewService.listComments(principal, page);

        // then
        assertEquals(commentsPage, result.getContents());

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(commentsService).findByUser(eq(user), eq(page), anyInt());
    }

    @Test
    public void listCommentsWithoutPrincipal() {
        // given
        when(principalHelper.getUserFromPrincipal(null, true))
                .thenThrow(IllegalArgumentException.class);

        int page = 1;

        // when/then
        assertThrows(IllegalArgumentException.class, () -> commentsViewService.listComments(null, page));

        verify(principalHelper).getUserFromPrincipal(null, true);

        verify(commentsService, never()).findByUser(any(User.class), anyInt(), anyInt());
    }
}
