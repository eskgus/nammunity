package com.eskgus.nammunity.service.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.enums.Fields;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private static final Long ID = 1L;
    private static final int PAGE = 1;
    private static final int SIZE = 3;
    private static final String CONTENT = Fields.CONTENT.getKey();

    @Test
    public void saveComments() {
        // given
        Posts post = mock(Posts.class);
        when(post.getId()).thenReturn(ID);

        CommentsSaveDto requestDto = new CommentsSaveDto(CONTENT, post.getId());

        Principal principal = mock(Principal.class);
        User user = mock(User.class);
        when(principalHelper.getUserFromPrincipal(principal, true)).thenReturn(user);

        when(postsService.findById(anyLong())).thenReturn(post);

        Comments comment = giveComment(ID);
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
    public void updateComments() {
        // given
        Comments comment = giveComment(ID);
        when(commentsRepository.findById(anyLong())).thenReturn(Optional.of(comment));

        // when
        Long result = commentsService.update(comment.getId(), CONTENT);

        // then
        assertEquals(comment.getId(), result);

        verify(commentsRepository).findById(comment.getId());
        verify(comment).update(eq(CONTENT));
    }

    @Test
    public void deleteSelectedComments() {
        // given
        List<Comments> comments = giveComments();

        List<Long> commentIds = comments.stream().map(Comments::getId).toList();

        when(commentsRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return comments.stream().filter(comment -> id.equals(comment.getId())).findFirst();
        });

        doNothing().when(commentsRepository).delete(any(Comments.class));

        // when
        commentsService.deleteSelectedComments(commentIds);

        // then
        verify(commentsRepository, times(commentIds.size())).findById(anyLong());
        verify(commentsRepository, times(comments.size())).delete(any(Comments.class));
    }

    @Test
    public void deleteComments() {
        // given
        Comments comment = giveComment(ID);
        when(commentsRepository.findById(anyLong())).thenReturn(Optional.of(comment));

        doNothing().when(commentsRepository).delete(any(Comments.class));

        // when
        commentsService.delete(comment.getId());

        // then
        verify(commentsRepository).findById(eq(comment.getId()));
        verify(commentsRepository).delete(eq(comment));
    }

    @Test
    public void findCommentsById() {
        // given
        Comments comment = giveComment(ID);
        when(commentsRepository.findById(anyLong())).thenReturn(Optional.of(comment));

        // when
        Comments result = commentsService.findById(comment.getId());

        // then
        assertEquals(comment, result);

        verify(commentsRepository).findById(eq(comment.getId()));
    }

    @Test
    public void findCommentsByPosts() {
        // given
        Posts post = mock(Posts.class);

        Page<CommentsReadDto> commentsPage = new PageImpl<>(Collections.emptyList());
        when(commentsRepository.findByPosts(any(Posts.class), any(Pageable.class))).thenReturn(commentsPage);

        // when
        Page<CommentsReadDto> result = commentsService.findByPosts(post, PAGE);

        // then
        assertEquals(commentsPage, result);

        verify(commentsRepository).findByPosts(eq(post), any(Pageable.class));
    }

    @Test
    public void findCommentsByUser() {
        // given
        User user = mock(User.class);

        Page<CommentsListDto> commentsPage = new PageImpl<>(Collections.emptyList());
        when(commentsRepository.findByUser(any(User.class), any(Pageable.class))).thenReturn(commentsPage);

        // when
        Page<CommentsListDto> result = commentsService.findByUser(user, PAGE, SIZE);

        // then
        assertEquals(commentsPage, result);

        verify(commentsRepository).findByUser(eq(user), any(Pageable.class));
    }

    @Test
    public void countCommentsByUser() {
        // given
        User user = mock(User.class);

        when(commentsRepository.countByUser(any(User.class))).thenReturn(ID);

        // when
        long result = commentsService.countByUser(user);

        // then
        assertEquals(ID, result);

        verify(commentsRepository).countByUser(eq(user));
    }

    @Test
    public void searchCommentsByContent() {
        // given
        String keywords = "keyword";

        Page<CommentsListDto> commentsPage = new PageImpl<>(Collections.emptyList());
        when(commentsRepository.searchByContent(any(String.class), any(Pageable.class))).thenReturn(commentsPage);

        // when
        Page<CommentsListDto> result = commentsService.searchByContent(keywords, PAGE, SIZE);

        // then
        assertEquals(commentsPage, result);

        verify(commentsRepository).searchByContent(eq(keywords), any(Pageable.class));
    }

    @Test
    public void calculateCommentPage() {
        // given
        Long postId = ID;
        Long commentId = 789L;

        long commentIndex = 123;
        when(commentsRepository.countCommentIndex(anyLong(), anyLong())).thenReturn(commentIndex);

        // when
        int result = commentsService.calculateCommentPage(postId, commentId);

        // then
        assertEquals(commentIndex / 30 + 1, result);

        verify(commentsRepository).countCommentIndex(eq(postId), eq(commentId));
    }

    private List<Comments> giveComments() {
        List<Comments> comments = new ArrayList<>();
        for (long i = 0; i < 3; i++) {
            Comments comment = giveComment(ID + i);
            comments.add(comment);
        }

        return comments;
    }

    private Comments giveComment(Long id) {
        Comments comment = mock(Comments.class);
        when(comment.getId()).thenReturn(id);

        return comment;
    }
}
