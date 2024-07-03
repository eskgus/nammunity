package com.eskgus.nammunity.service.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.likes.LikesService;
import com.eskgus.nammunity.util.ServiceTestUtil;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.util.Pair;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private static final Long ID = 1L;
    private static final int PAGE = 1;

    @Test
    public void findCommentsPageByPosts() {
        // given
        Posts post = mock(Posts.class);

        User user = ServiceTestUtil.giveUserId(ID);

        CommentsReadDto commentsReadDto = mock(CommentsReadDto.class);
        when(commentsReadDto.getAuthorId()).thenReturn(ID);
        when(commentsReadDto.getId()).thenReturn(ID);
        boolean doesUserWriteComment = commentsReadDto.getAuthorId().equals(user.getId());

        Page<CommentsReadDto> commentsPage = ServiceTestUtil.giveContentsPage(
                commentsService::findByPosts, commentsReadDto);

        Comments comment = ServiceTestUtil.giveComment(commentsService::findById);

        boolean doesUserLikeComment = false;
        when(likesService.existsByCommentsAndUser(any(Comments.class), any(User.class))).thenReturn(doesUserLikeComment);

        // when
        Page<CommentsReadDto> result = commentsViewService.findCommentsPageByPosts(post, user, PAGE);

        // then
        assertEquals(commentsPage, result);

        verify(commentsService).findByPosts(eq(post), eq(PAGE));
        verify(commentsReadDto).setDoesUserWriteComment(doesUserWriteComment);
        verify(commentsService).findById(eq(commentsReadDto.getId()));
        verify(likesService).existsByCommentsAndUser(eq(comment), eq(user));
        verify(commentsReadDto).setDoesUserLikeComment(doesUserLikeComment);
    }

    @Test
    public void listComments() {
        // given
        Pair<Principal, User> pair = ServiceTestUtil.givePrincipal(principalHelper::getUserFromPrincipal);
        Principal principal = pair.getFirst();
        User user = pair.getSecond();

        Page<CommentsListDto> commentsPage = ServiceTestUtil.giveContentsPage(commentsService::findByUser, User.class);

        // when
        ContentsPageDto<CommentsListDto> result = commentsViewService.listComments(principal, PAGE);

        // then
        assertEquals(commentsPage, result.getContents());

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(commentsService).findByUser(eq(user), eq(PAGE), anyInt());
    }
}
