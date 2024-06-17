package com.eskgus.nammunity.service.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.comments.CommentsSaveDto;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentsServiceTest {
    @Mock
    private CommentsRepository commentsRepository;

    @Mock
    private PostsService postsService;

    @Mock
    private PrincipalHelper principalHelper;

    @InjectMocks
    private CommentsService commentsService;

    @Test
    public void save() {
        // given
        Posts post = mock(Posts.class);
        when(post.getId()).thenReturn(1L);
        when(postsService.findById(post.getId())).thenReturn(post);

        CommentsSaveDto requestDto = new CommentsSaveDto("comment", post.getId());

        Principal principal = mock(Principal.class);
        User user = mock(User.class);
        when(principalHelper.getUserFromPrincipal(principal, true)).thenReturn(user);

        Comments comment = mock(Comments.class);
        when(comment.getId()).thenReturn(1L);
        when(commentsRepository.save(any(Comments.class))).thenReturn(comment);

        // when
        Long result = commentsService.save(requestDto, principal);

        // then
        assertEquals(comment.getId(), result);

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(postsService).findById(eq(post.getId()));
        verify(commentsRepository).save(any(Comments.class));
    }

    @Test
    public void saveWithoutPrincipal() {
        // given
        Posts post = mock(Posts.class);
        when(post.getId()).thenReturn(1L);

        CommentsSaveDto requestDto = new CommentsSaveDto("comment", post.getId());

        when(principalHelper.getUserFromPrincipal(null, true))
                .thenThrow(IllegalArgumentException.class);

        // when/then
        assertThrows(IllegalArgumentException.class, () -> commentsService.save(requestDto, null));

        verify(principalHelper).getUserFromPrincipal(null, true);

        verify(postsService, never()).findById(anyLong());
        verify(commentsRepository, never()).save(any(Comments.class));
    }

    @Test
    public void findByUser() {
        // given
        Page<CommentsListDto> commentsPage = new PageImpl<>(Collections.emptyList());
        when(commentsRepository.findByUser(any(User.class), any(Pageable.class))).thenReturn(commentsPage);

        User user = mock(User.class);
        int page = 1;
        int size = 4;

        // when
        Page<CommentsListDto> result = commentsService.findByUser(user, page, size);

        // then
        assertEquals(commentsPage, result);

        verify(commentsRepository).findByUser(eq(user), any(Pageable.class));
    }

    @Test
    public void searchByContent() {
        // given
        Page<CommentsListDto> commentsPage = new PageImpl<>(Collections.emptyList());
        when(commentsRepository.searchByContent(anyString(), any(Pageable.class))).thenReturn(commentsPage);

        String keywords = "keyword";
        int page = 1;
        int size = 4;

        // when
        Page<CommentsListDto> result = commentsService.searchByContent(keywords, page, size);

        // then
        assertEquals(commentsPage, result);

        verify(commentsRepository).searchByContent(eq(keywords), any(Pageable.class));
    }

    @Test
    public void calculateCommentPage() {
        // given
        long commentIndex = 55;
        when(commentsRepository.countCommentIndex(anyLong(), anyLong())).thenReturn(commentIndex);

        Long postId = 1L;
        Long commentId = 123L;

        // when
        int result = commentsService.calculateCommentPage(postId, commentId);

        // then
        assertEquals(commentIndex / 30 + 1, result);

        verify(commentsRepository).countCommentIndex(eq(postId), eq(commentId));
    }
}
