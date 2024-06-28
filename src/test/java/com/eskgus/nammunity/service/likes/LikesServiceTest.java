package com.eskgus.nammunity.service.likes;

import com.eskgus.nammunity.converter.LikesConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.util.ServiceTestUtil;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import static com.eskgus.nammunity.util.ServiceTestUtil.createContentIds;
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

    private static final Long ID = 1L;

    @Test
    public void savePostLikes() {
        Posts post = givePost();

        testSaveLikes(post.getId(), null);

        verify(postsService).findById(eq(post.getId()));
    }

    @Test
    public void saveCommentLikes() {
        Comments comment = giveComment();

        testSaveLikes(null, comment.getId());

        verify(commentsService).findById(eq(comment.getId()));
    }

    @Test
    public void deleteLikesByPostId() {
        // given
        Posts post = givePost();

        doNothing().when(likesRepository).deleteByPosts(any(Posts.class), any(User.class));

        // when/then
        User user = testDeleteLikesByContentId(post.getId(), null);

        verify(postsService).findById(eq(post.getId()));
        verify(likesRepository).deleteByPosts(eq(post), eq(user));
    }

    @Test
    public void deleteLikesByCommentId() {
        // given
        Comments comment = giveComment();

        doNothing().when(likesRepository).deleteByComments(any(Comments.class), any(User.class));

        // when/then
        User user = testDeleteLikesByContentId(null, comment.getId());

        verify(commentsService).findById(eq(comment.getId()));
        verify(likesRepository).deleteByComments(eq(comment), eq(user));
    }

    @Test
    public void deleteSelectedLikes() {
        // given
        List<Likes> likes = giveLikes();

        List<Long> likeIds = createContentIds(likes, new LikesConverterForTest());

        when(likesRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return likes.stream().filter(like -> id.equals(like.getId())).findFirst();
        });

        doNothing().when(likesRepository).delete(any(Likes.class));

        // when
        likesService.deleteSelectedLikes(likeIds);

        // then
        verify(likesRepository, times(likeIds.size())).findById(anyLong());
        verify(likesRepository, times(likes.size())).delete(any(Likes.class));
    }

    @Test
    public void findLikesByUser() {
        User user = testFindLikesByUser(likesRepository::findByUser);

        verify(likesRepository).findByUser(eq(user), any(Pageable.class));
    }

    @Test
    public void findPostLikesByUser() {
        User user = testFindLikesByUser(likesRepository::findPostLikesByUser);

        verify(likesRepository).findPostLikesByUser(eq(user), any(Pageable.class));
    }

    @Test
    public void findCommentLikesByUser() {
        User user = testFindLikesByUser(likesRepository::findCommentLikesByUser);

        verify(likesRepository).findCommentLikesByUser(eq(user), any(Pageable.class));
    }

    @Test
    public void existsLikesByPostsAndUser() {
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
    public void existsLikesByCommentsAndUser() {
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

    private Posts givePost() {
        return ServiceTestUtil.givePost(ID, postsService::findById);
    }

    private Comments giveComment() {
        return ServiceTestUtil.giveComment(ID, commentsService::findById);
    }

    private Pair<Principal, User> givePrincipal() {
        return ServiceTestUtil.givePrincipal(principalHelper::getUserFromPrincipal);
    }

    private List<Likes> giveLikes() {
        List<Likes> likes = new ArrayList<>();
        for (long i = 0; i < 3; i++) {
            Likes like = giveLike(ID + i);
            likes.add(like);
        }

        return likes;
    }

    private Likes giveLike(Long id) {
        Likes like = mock(Likes.class);
        when(like.getId()).thenReturn(id);

        return like;
    }

    private void testSaveLikes(Long postId, Long commentId) {
        // given
        Principal principal = givePrincipal().getFirst();

        Likes like = giveLike(ID);
        when(likesRepository.save(any(Likes.class))).thenReturn(like);

        // when
        Long result = likesService.save(postId, commentId, principal);

        // then
        assertEquals(like.getId(), result);

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(likesRepository).save(any(Likes.class));
    }

    private User testDeleteLikesByContentId(Long postId, Long commentId) {
        Pair<Principal, User> pair = givePrincipal();
        Principal principal = pair.getFirst();

        // when
        likesService.deleteByContentId(postId, commentId, principal);

        // then
        verify(principalHelper).getUserFromPrincipal(principal, true);

        return pair.getSecond();
    }

    private User testFindLikesByUser(BiFunction<User, Pageable, Page<LikesListDto>> finder) {
        // given
        User user = mock(User.class);

        int page = 1;
        int size = 3;

        Page<LikesListDto> likesPage = new PageImpl<>(Collections.emptyList());
        when(finder.apply(any(User.class), any(Pageable.class))).thenReturn(likesPage);

        // when
        Page<LikesListDto> result = likesService.findLikesByUser(user, finder, page, size);

        // then
        assertEquals(likesPage, result);

        return user;
    }
}
