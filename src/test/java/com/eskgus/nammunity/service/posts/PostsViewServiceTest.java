package com.eskgus.nammunity.service.posts;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.comments.CommentsViewService;
import com.eskgus.nammunity.service.likes.LikesService;
import com.eskgus.nammunity.service.reports.ReasonsService;
import com.eskgus.nammunity.util.ServiceTestUtil;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.posts.PostWithReasonsDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.posts.PostsUpdateDto;
import com.eskgus.nammunity.web.dto.reports.ReasonsListDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.util.Pair;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.eskgus.nammunity.domain.enums.Fields.CONTENT;
import static com.eskgus.nammunity.domain.enums.Fields.TITLE;
import static com.eskgus.nammunity.util.ServiceTestUtil.givePrincipal;
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

    private static final Long ID = 1L;
    private static final int PAGE = 1;

    @Test
    public void readPostsWithPostAuthor() {
        Principal principal = mock(Principal.class);
        testReadPosts(principal, true, never());
    }

    @Test
    public void readPostsWithAnonymousUser() {
        testReadPosts(null, false, times(1));
    }

    @Test
    public void readCommentsWithAuthenticatedUser() {
        User user = mock(User.class);
        testReadComments(user);
    }

    @Test
    public void readCommentsWithAnonymousUser() {
        testReadComments(null);
    }

    @Test
    public void listPosts() {
        // given
        Pair<Principal, User> pair = givePrincipal(principalHelper::getUserFromPrincipal);
        Principal principal = pair.getFirst();
        User user = pair.getSecond();

        Page<PostsListDto> postsPage = new PageImpl<>(Collections.emptyList());
        when(postsService.findByUser(any(User.class), anyInt(), anyInt())).thenReturn(postsPage);

        // when
        ContentsPageDto<PostsListDto> result = postsViewService.listPosts(principal, PAGE);

        // then
        assertEquals(postsPage, result.getContents());

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(postsService).findByUser(eq(user), eq(PAGE), anyInt());
    }

    @Test
    public void updatePosts() {
        // given
        Posts post = givePost();
        when(post.getTitle()).thenReturn(TITLE.getKey());
        when(post.getContent()).thenReturn(CONTENT.getKey());

        // when
        PostsUpdateDto result = postsViewService.updatePosts(post.getId());

        // then
        assertEquals(post.getId(), result.getId());
        assertEquals(post.getTitle(), result.getTitle());
        assertEquals(post.getContent(), result.getContent());

        verify(postsService).findById(eq(post.getId()));
    }

    private Posts givePostDates() {
        Posts post = givePost();

        LocalDateTime now = LocalDateTime.now();
        when(post.getCreatedDate()).thenReturn(now);
        when(post.getModifiedDate()).thenReturn(now);

        return post;
    }

    private Posts givePost() {
        return ServiceTestUtil.givePost(ID, postsService::findById);
    }

    private User giveUser(Posts post) {
        User user = ServiceTestUtil.giveUser(ID);
        when(post.getUser()).thenReturn(user);

        return user;
    }

    private void testReadPosts(Principal principal, boolean userWritesOrLikesPost, VerificationMode mode) {
        // given
        Posts post = givePostDates();

        User author = giveUser(post);

        User user = principal != null ? author : null;
        when(principalHelper.getUserFromPrincipal(principal, false)).thenReturn(user);

        when(likesService.existsByPostsAndUser(any(Posts.class), eq(user))).thenReturn(userWritesOrLikesPost);

        List<ReasonsListDto> reasonsListDtos = Collections.emptyList();
        when(reasonsService.findAllAsc()).thenReturn(reasonsListDtos);

        // when
        PostWithReasonsDto result = postsViewService.readPosts(post.getId(), principal);

        // then
        assertNotNull(result);
        assertEquals(post.getId(), result.getPost().getId());
        assertEquals(userWritesOrLikesPost, result.getPost().isPostedByUser());
        assertEquals(userWritesOrLikesPost, result.getPost().isLikedByUser());
        assertEquals(reasonsListDtos, result.getReasons());

        verify(postsService).findById(eq(post.getId()));
        verify(principalHelper).getUserFromPrincipal(principal, false);
        verify(postsService, mode).countView(eq(post));
        verify(likesService).existsByPostsAndUser(eq(post), eq(user));
        verify(reasonsService).findAllAsc();
    }

    private void testReadComments(User user) {
        // given
        Posts post = givePost();

        Principal principal = mock(Principal.class);
        when(principalHelper.getUserFromPrincipal(principal, false)).thenReturn(user);

        Page<CommentsReadDto> commentsPage = new PageImpl<>(Collections.emptyList());
        when(commentsViewService.findCommentsPageByPosts(any(Posts.class), eq(user), anyInt()))
                .thenReturn(commentsPage);

        // when
        ContentsPageDto<CommentsReadDto> result = postsViewService.readComments(post.getId(), principal, PAGE);

        // then
        assertEquals(commentsPage, result.getContents());

        verify(postsService).findById(eq(post.getId()));
        verify(principalHelper).getUserFromPrincipal(principal, false);
        verify(commentsViewService).findCommentsPageByPosts(eq(post), eq(user), eq(PAGE));
    }
}
