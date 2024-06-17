package com.eskgus.nammunity.service.likes;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.util.Collections;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LikesServiceTest {
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

    @Test
    public void savePostLikes() {
        Posts post = mock(Posts.class);
        when(post.getId()).thenReturn(1L);
        when(postsService.findById(anyLong())).thenReturn(post);

        testSaveLikes(post.getId(), null);

        verify(postsService).findById(eq(post.getId()));
        verify(commentsService, never()).findById(anyLong());
    }

    @Test
    public void saveCommentLikes() {
        Comments comment = mock(Comments.class);
        when(comment.getId()).thenReturn(1L);
        when(commentsService.findById(anyLong())).thenReturn(comment);

        testSaveLikes(null, comment.getId());

        verify(postsService, never()).findById(anyLong());
        verify(commentsService).findById(eq(comment.getId()));
    }

    @Test
    public void saveWithoutPrincipal() {
        // given
        when(principalHelper.getUserFromPrincipal(null, true))
                .thenThrow(IllegalArgumentException.class);

        // when/then
        assertThrows(IllegalArgumentException.class, () -> likesService.save(1L, null, null));

        verify(principalHelper).getUserFromPrincipal(null, true);

        verify(postsService, never()).findById(anyLong());
        verify(commentsService, never()).findById(anyLong());
        verify(likesRepository, never()).save(any(Likes.class));
    }

    @Test
    public void savePostLikesWithNonExistentPostId() {
        Posts post = mock(Posts.class);
        when(post.getId()).thenReturn(1L);
        when(postsService.findById(anyLong())).thenThrow(IllegalArgumentException.class);

        testSaveLikesWithNonExistentContentId(post.getId(), null);

        verify(postsService).findById(eq(post.getId()));

        verify(commentsService, never()).findById(anyLong());
    }

    @Test
    public void saveCommentLikesWithNonExistentPostId() {
        Comments comment = mock(Comments.class);
        when(comment.getId()).thenReturn(1L);
        when(commentsService.findById(anyLong())).thenThrow(IllegalArgumentException.class);

        testSaveLikesWithNonExistentContentId(null, comment.getId());

        verify(commentsService).findById(eq(comment.getId()));

        verify(postsService, never()).findById(anyLong());
    }

    @Test
    public void deletePostLikes() {
        Posts post = mock(Posts.class);
        when(post.getId()).thenReturn(1L);
        when(postsService.findById(anyLong())).thenReturn(post);

        User user = testDeleteByContentId(post.getId(), null);

        verify(postsService).findById(eq(post.getId()));
        verify(likesRepository).deleteByPosts(eq(post), eq(user));
    }

    @Test
    public void deleteCommentLikes() {
        Comments comment = mock(Comments.class);
        when(comment.getId()).thenReturn(1L);
        when(commentsService.findById(anyLong())).thenReturn(comment);

        User user = testDeleteByContentId(null, comment.getId());

        verify(commentsService).findById(eq(comment.getId()));
        verify(likesRepository).deleteByComments(eq(comment), eq(user));
    }

    @Test
    public void deleteWithoutPrincipal() {
        // given
        when(principalHelper.getUserFromPrincipal(null, true))
                .thenThrow(IllegalArgumentException.class);

        // when/then
        assertThrows(IllegalArgumentException.class,
                () -> likesService.deleteByContentId(1L, null, null));

        verify(principalHelper).getUserFromPrincipal(null, true);

        verify(postsService, never()).findById(anyLong());
        verify(likesRepository, never()).deleteByPosts(any(Posts.class), any(User.class));
        verify(commentsService, never()).findById(anyLong());
        verify(likesRepository, never()).deleteByComments(any(Comments.class), any(User.class));
    }

    @Test
    public void deletePostLikesWithNonExistentPostId() {
        Posts post = mock(Posts.class);
        when(post.getId()).thenReturn(1L);
        when(postsService.findById(anyLong())).thenThrow(IllegalArgumentException.class);

        testDeleteByNonExistentContentId(post.getId(), null);

        verify(postsService).findById(post.getId());

        verify(commentsService, never()).findById(anyLong());
    }

    @Test
    public void deleteCommentLikesWithNonExistentCommentId() {
        Comments comment = mock(Comments.class);
        when(comment.getId()).thenReturn(1L);
        when(commentsService.findById(anyLong())).thenThrow(IllegalArgumentException.class);

        testDeleteByNonExistentContentId(null, comment.getId());

        verify(commentsService).findById(eq(comment.getId()));

        verify(postsService, never()).findById(anyLong());
    }

    @Test
    public void findLikesByUser() {
        // given
        User user = mock(User.class);

        BiFunction<User, Pageable, Page<LikesListDto>> finder = mock(BiFunction.class);

        Page<LikesListDto> likesPage = new PageImpl<>(Collections.emptyList());
        when(finder.apply(any(User.class), any(Pageable.class))).thenReturn(likesPage);

        int page = 1;
        int size = 4;

        // when
        Page<LikesListDto> result = likesService.findLikesByUser(user, finder, page, size);

        // then
        assertEquals(likesPage, result);

        verify(finder).apply(eq(user), any(Pageable.class));
    }

    @Test
    public void existsByPostsAndUser() {
        // given
        Posts post = mock(Posts.class);
        User user = mock(User.class);

        when(likesRepository.existsByPostsAndUser(any(Posts.class), any(User.class))).thenReturn(true);

        // when
        boolean result = likesService.existsByPostsAndUser(post, user);

        // then
        assertTrue(result);

        verify(likesRepository).existsByPostsAndUser(eq(post), eq(user));
    }

    @Test
    public void existsByCommentsAndUser() {
        // given
        Comments comment = mock(Comments.class);
        User user = mock(User.class);

        when(likesRepository.existsByCommentsAndUser(any(Comments.class), any(User.class))).thenReturn(true);

        // when
        boolean result = likesService.existsByCommentsAndUser(comment, user);

        // then
        assertTrue(result);

        verify(likesRepository).existsByCommentsAndUser(eq(comment), eq(user));
    }

    private void testSaveLikes(Long postId, Long commentId) {
        // given
        Principal principal = mock(Principal.class);
        User user = mock(User.class);
        when(principalHelper.getUserFromPrincipal(principal, true)).thenReturn(user);

        Likes like = mock(Likes.class);
        when(like.getId()).thenReturn(1L);
        when(likesRepository.save(any(Likes.class))).thenReturn(like);

        // when
        Long result = likesService.save(postId, commentId, principal);

        // then
        assertEquals(like.getId(), result);

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(likesRepository).save(any(Likes.class));
    }

    private void testSaveLikesWithNonExistentContentId(Long postId, Long commentId) {
        // given
        Principal principal = mock(Principal.class);
        User user = mock(User.class);
        when(principalHelper.getUserFromPrincipal(principal, true)).thenReturn(user);

        // when/then
        assertThrows(IllegalArgumentException.class, () -> likesService.save(postId, commentId, principal));

        verify(principalHelper).getUserFromPrincipal(principal, true);

        verify(likesRepository, never()).save(any(Likes.class));
    }

    private User testDeleteByContentId(Long postId, Long commentId) {
        // given
        Principal principal = mock(Principal.class);
        User user = mock(User.class);
        when(principalHelper.getUserFromPrincipal(principal, true)).thenReturn(user);

        // when
        likesService.deleteByContentId(postId, commentId, principal);

        // then
        verify(principalHelper).getUserFromPrincipal(principal, true);

        return user;
    }

    private void testDeleteByNonExistentContentId(Long postId, Long commentId) {
        // given
        Principal principal = mock(Principal.class);
        User user = mock(User.class);
        when(principalHelper.getUserFromPrincipal(principal, true)).thenReturn(user);

        // when/then
        assertThrows(IllegalArgumentException.class,
                () -> likesService.deleteByContentId(postId, commentId, principal));

        verify(principalHelper).getUserFromPrincipal(principal, true);

        verify(likesRepository, never()).deleteByPosts(any(Posts.class), any(User.class));
        verify(likesRepository, never()).deleteByComments(any(Comments.class), any(User.class));
    }
}
