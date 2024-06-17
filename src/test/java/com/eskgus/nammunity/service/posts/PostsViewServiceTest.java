package com.eskgus.nammunity.service.posts;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.comments.CommentsViewService;
import com.eskgus.nammunity.service.likes.LikesService;
import com.eskgus.nammunity.service.reports.ReasonsService;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.posts.PostWithReasonsDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.reports.ReasonsListDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostsViewServiceTest {
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

    @Test
    public void readPosts() {
        // given
        Posts post = mock(Posts.class);
        when(post.getId()).thenReturn(1L);
        when(post.getCreatedDate()).thenReturn(LocalDateTime.now());
        when(post.getModifiedDate()).thenReturn(LocalDateTime.now());
        when(postsService.findById(post.getId())).thenReturn(post);

        User author = mock(User.class);
        when(post.getUser()).thenReturn(author);
        when(author.getId()).thenReturn(1L);

        Principal principal = mock(Principal.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn(2L);
        when(principalHelper.getUserFromPrincipal(principal, false)).thenReturn(user);

        when(likesService.existsByPostsAndUser(any(Posts.class), any(User.class))).thenReturn(true);

        List<ReasonsListDto> reasonsListDtos = Collections.emptyList();
        when(reasonsService.findAllAsc()).thenReturn(reasonsListDtos);

        // when
        PostWithReasonsDto result = postsViewService.readPosts(post.getId(), principal);

        // then
        assertNotNull(result);
        assertEquals(post.getId(), result.getPost().getId());
        assertFalse(result.getPost().isDoesUserWritePost());
        assertTrue(result.getPost().isDoesUserLikePost());
        assertEquals(reasonsListDtos, result.getReasons());

        verify(postsService).findById(eq(post.getId()));
        verify(principalHelper).getUserFromPrincipal(principal, false);
        verify(postsService).countView(eq(post));
        verify(likesService).existsByPostsAndUser(eq(post), eq(user));
        verify(reasonsService).findAllAsc();
    }

    @Test
    public void readPostsWithNonExistentPostId() {
        // given
        Posts post = mock(Posts.class);
        when(post.getId()).thenReturn(1L);
        when(postsService.findById(anyLong())).thenThrow(IllegalArgumentException.class);

        Principal principal = mock(Principal.class);

        // when/then
        assertThrows(IllegalArgumentException.class, () -> postsViewService.readPosts(1L, principal));

        verify(postsService).findById(eq(post.getId()));

        verify(principalHelper, never()).getUserFromPrincipal(any(Principal.class), anyBoolean());
        verify(postsService, never()).countView(any(Posts.class));
        verify(likesService, never()).existsByPostsAndUser(any(Posts.class), any(User.class));
        verify(reasonsService, never()).findAllAsc();
    }

    @Test
    public void readComments() {
        // given
        Posts post = mock(Posts.class);
        when(post.getId()).thenReturn(1L);
        when(postsService.findById(post.getId())).thenReturn(post);

        Principal principal = mock(Principal.class);
        User user = mock(User.class);
        when(principalHelper.getUserFromPrincipal(principal, false)).thenReturn(user);

        Page<CommentsReadDto> commentsPage = new PageImpl<>(Collections.emptyList());
        when(commentsViewService.findCommentsPageByPosts(any(Posts.class), any(User.class), anyInt()))
                .thenReturn(commentsPage);

        int page = 1;

        // when
        ContentsPageDto<CommentsReadDto> result = postsViewService.readComments(post.getId(), principal, page);

        // then
        assertEquals(commentsPage, result.getContents());

        verify(postsService).findById(eq(post.getId()));
        verify(principalHelper).getUserFromPrincipal(principal, false);
        verify(commentsViewService).findCommentsPageByPosts(eq(post), eq(user), eq(page));
    }

    @Test
    public void readCommentsWithNonExistentPostId() {
        // given
        Posts post = mock(Posts.class);
        when(post.getId()).thenReturn(1L);
        when(postsService.findById(anyLong())).thenThrow(IllegalArgumentException.class);

        Principal principal = mock(Principal.class);

        int page = 1;

        // when/then
        assertThrows(IllegalArgumentException.class, () -> postsViewService.readComments(post.getId(), principal, page));

        verify(postsService).findById(eq(post.getId()));

        verify(principalHelper, never()).getUserFromPrincipal(any(Principal.class), anyBoolean());
        verify(commentsViewService, never()).findCommentsPageByPosts(any(Posts.class), any(User.class), anyInt());
    }

    @Test
    public void readCommentsWithNonExistentCommentId() {
        // given
        Posts post = mock(Posts.class);
        when(post.getId()).thenReturn(1L);
        when(postsService.findById(post.getId())).thenReturn(post);

        Principal principal = mock(Principal.class);
        User user = mock(User.class);
        when(principalHelper.getUserFromPrincipal(principal, false)).thenReturn(user);

        when(commentsViewService.findCommentsPageByPosts(any(Posts.class), any(User.class), anyInt()))
                .thenThrow(IllegalArgumentException.class);

        int page = 1;

        // when/then
        assertThrows(IllegalArgumentException.class, () -> postsViewService.readComments(post.getId(), principal, page));

        verify(postsService).findById(eq(post.getId()));
        verify(principalHelper).getUserFromPrincipal(principal, false);
        verify(commentsViewService).findCommentsPageByPosts(eq(post), eq(user), eq(page));
    }

    @Test
    public void listPosts() {
        // given
        Principal principal = mock(Principal.class);
        User user = mock(User.class);
        when(principalHelper.getUserFromPrincipal(principal, true)).thenReturn(user);

        Page<PostsListDto> postsPage = new PageImpl<>(Collections.emptyList());
        when(postsService.findByUser(any(User.class), anyInt(), anyInt())).thenReturn(postsPage);

        int page = 1;

        // when
        ContentsPageDto<PostsListDto> result = postsViewService.listPosts(principal, page);

        // then
        assertEquals(postsPage, result.getContents());

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(postsService).findByUser(eq(user), eq(page), anyInt());
    }

    @Test
    public void listPostsWithoutPrincipal() {
        // given
        when(principalHelper.getUserFromPrincipal(null, true))
                .thenThrow(IllegalArgumentException.class);

        int page = 1;

        // when/then
        assertThrows(IllegalArgumentException.class, () -> postsViewService.listPosts(null, page));

        // then
        verify(principalHelper).getUserFromPrincipal(null, true);

        verify(postsService, never()).findByUser(any(User.class), anyInt(), anyInt());
    }
}
